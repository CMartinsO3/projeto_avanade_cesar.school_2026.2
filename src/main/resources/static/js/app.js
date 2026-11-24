// =====================================================================
// PetCheck-in · front-end em JavaScript puro (sem frameworks)
// O front é servido pelo próprio Spring Boot, então a API está na mesma
// origem: basta chamar "/api/...".
// =====================================================================

const TAMANHO_MAXIMO = 10 * 1024 * 1024; // 10MB, o mesmo limite do back-end
const TIPOS_ACEITOS = ["application/pdf", "image/png", "image/jpeg"];

const ROTULOS_STATUS = {
    AGUARDANDO_DOCUMENTOS: "Aguardando documentos",
    CONFIRMADA: "Confirmada",
    RECUSADA: "Recusada",
    EM_ANALISE_MANUAL: "Em análise",
    APTO: "Apto",
    NAO_APTO: "Não apto"
};

// Dados carregados da API e o arquivo escolhido pelo usuário
const estado = {
    pets: [],
    reservas: [],
    arquivo: null
};

// Atalho para pegar elementos pelo id
const $ = (id) => document.getElementById(id);


// =====================================================================
// Funções de apoio
// =====================================================================

/** Chama a API e devolve o JSON. Se der erro, lança a mensagem que veio do back-end. */
async function api(caminho, opcoes = {}) {
    let resposta;
    try {
        resposta = await fetch("/api" + caminho, opcoes);
    } catch (e) {
        throw new Error("Não foi possível conectar à API. Ela está rodando?");
    }
    const corpo = await resposta.json().catch(() => null);
    if (!resposta.ok) {
        throw criarErro(resposta.status, corpo);
    }
    return corpo;
}

/** Envia JSON com POST. */
function postarJson(caminho, dados) {
    return api(caminho, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dados)
    });
}

/** Transforma a resposta de erro da API ({ status, erro, campos }) em uma mensagem legível. */
function criarErro(status, corpo) {
    let mensagem = corpo?.erro || `Erro ${status} ao falar com a API.`;
    if (corpo?.campos) {
        mensagem += "\n" + Object.values(corpo.campos).map((m) => "• " + m).join("\n");
    }
    const erro = new Error(mensagem);
    erro.status = status;
    return erro;
}

/** "2026-12-01" → "01/12/2026". A API usa ISO 8601; o formato brasileiro fica só na tela. */
function formatarData(iso) {
    if (!iso) {
        return "-";
    }
    const [ano, mes, dia] = iso.substring(0, 10).split("-");
    return `${dia}/${mes}/${ano}`;
}

/** "2026-11-24T15:37:03.36" → "24/11/2026 às 15:37" */
function formatarDataHora(iso) {
    return `${formatarData(iso)} às ${iso.substring(11, 16)}`;
}

function formatarTamanho(bytes) {
    if (bytes < 1024 * 1024) {
        return (bytes / 1024).toFixed(0) + " KB";
    }
    return (bytes / 1024 / 1024).toFixed(1).replace(".", ",") + " MB";
}

/**
 * Protege contra HTML malicioso: o nome de um pet como "<script>" vira texto, e não código.
 * Sempre que um dado da API ou do usuário entra no innerHTML, ele passa por aqui.
 */
function esc(texto) {
    const div = document.createElement("div");
    div.textContent = texto ?? "";
    return div.innerHTML;
}

function icone(nome) {
    return `<svg aria-hidden="true"><use href="#i-${nome}"/></svg>`;
}

function iconeEspecie(especie) {
    return icone(especie === "GATO" ? "gato" : "cao");
}

function selo(status) {
    return `<span class="selo selo-${esc(status)}">${esc(ROTULOS_STATUS[status] || status)}</span>`;
}

let temporizadorAviso;

/** Mostra uma mensagem flutuante no canto da tela por alguns segundos. */
function mostrarAviso(mensagem, tipo = "") {
    const aviso = $("aviso");
    aviso.textContent = mensagem;
    aviso.className = `aviso visivel ${tipo}`;
    clearTimeout(temporizadorAviso);
    temporizadorAviso = setTimeout(() => aviso.classList.remove("visivel"), 4500);
}


// =====================================================================
// Navegação entre as páginas do menu
// =====================================================================

function mostrarPagina(nome) {
    document.querySelectorAll(".pagina").forEach((pagina) => {
        pagina.classList.toggle("ativa", pagina.id === "pagina-" + nome);
    });
    document.querySelectorAll(".menu-item").forEach((item) => {
        item.classList.toggle("ativo", item.dataset.pagina === nome);
    });
    if (nome === "resultados") {
        carregarResultados();
    }
    window.scrollTo(0, 0);
}

document.querySelectorAll(".menu-item").forEach((item) => {
    item.addEventListener("click", () => mostrarPagina(item.dataset.pagina));
});


// =====================================================================
// Pets
// =====================================================================

async function carregarPets() {
    estado.pets = await api("/pets");

    $("lista-pets").innerHTML = estado.pets.length === 0
        ? `<p class="vazio">Nenhum pet cadastrado ainda.</p>`
        : estado.pets.map((pet) => `
            <div class="cartao pet-cartao">
                <span class="pet-avatar">${iconeEspecie(pet.especie)}</span>
                <div>
                    <strong>${esc(pet.nome)}</strong>
                    <small>${esc(pet.raca || (pet.especie === "GATO" ? "Gato" : "Cachorro"))}</small>
                    <small>Tutor: ${esc(pet.nomeTutor)}</small>
                    <small>Nascimento: ${formatarData(pet.dataNascimento)}</small>
                </div>
            </div>`).join("");

    $("reserva-pet").innerHTML = estado.pets
        .map((pet) => `<option value="${pet.id}">${esc(pet.nome)} (${esc(pet.nomeTutor)})</option>`)
        .join("");
}

$("form-pet").addEventListener("submit", async (evento) => {
    evento.preventDefault();
    const formulario = evento.target;
    const dados = Object.fromEntries(new FormData(formulario));
    dados.raca = dados.raca || null;
    dados.dataNascimento = dados.dataNascimento || null;

    try {
        const pet = await postarJson("/pets", dados);
        formulario.reset();
        mostrarAviso(`${pet.nome} foi cadastrado! 🐾`, "sucesso");
        await carregarPets();
    } catch (erro) {
        mostrarAviso(erro.message, "erro");
    }
});


// =====================================================================
// Reservas
// =====================================================================

async function carregarReservas() {
    estado.reservas = await api("/reservas");

    $("lista-reservas").innerHTML = estado.reservas.length === 0
        ? `<tr><td colspan="6" class="vazio">Nenhuma reserva cadastrada ainda.</td></tr>`
        : estado.reservas.map((reserva) => `
            <tr>
                <td>${reserva.id}</td>
                <td><span class="pet-nome">${iconeEspecie(reserva.pet.especie)} ${esc(reserva.pet.nome)}</span></td>
                <td>${formatarData(reserva.dataCheckin)}</td>
                <td>${formatarData(reserva.dataCheckout)}</td>
                <td>${selo(reserva.status)}</td>
                <td><button class="botao botao-secundario botao-pequeno" data-enviar="${reserva.id}">Enviar carteira</button></td>
            </tr>`).join("");

    preencherSelectReservas();
}

function preencherSelectReservas() {
    const select = $("reserva-select");
    const selecionada = select.value;

    if (estado.reservas.length === 0) {
        select.innerHTML = `<option value="">Nenhuma reserva: cadastre uma no menu Reservas</option>`;
    } else {
        select.innerHTML = `<option value="">Selecione a reserva...</option>` + estado.reservas
            .map((reserva) => `<option value="${reserva.id}">
                #${reserva.id} · ${esc(reserva.pet.nome)} · ${formatarData(reserva.dataCheckin)} a ${formatarData(reserva.dataCheckout)}
            </option>`).join("");
        select.value = selecionada;
    }
    atualizarResumoReserva();
}

// Botão "Enviar carteira" da tabela: abre a tela de envio com a reserva já escolhida
$("lista-reservas").addEventListener("click", (evento) => {
    const botao = evento.target.closest("[data-enviar]");
    if (botao) {
        reiniciarEnvio();
        $("reserva-select").value = botao.dataset.enviar;
        atualizarResumoReserva();
        mostrarPagina("documentos");
    }
});

$("form-reserva").addEventListener("submit", async (evento) => {
    evento.preventDefault();
    const formulario = evento.target;
    const dados = Object.fromEntries(new FormData(formulario));
    dados.petId = dados.petId ? Number(dados.petId) : null;
    dados.dataCheckin = dados.dataCheckin || null;
    dados.dataCheckout = dados.dataCheckout || null;

    try {
        const reserva = await postarJson("/reservas", dados);
        formulario.reset();
        mostrarAviso(`Reserva #${reserva.id} de ${reserva.pet.nome} cadastrada!`, "sucesso");
        await carregarReservas();
    } catch (erro) {
        mostrarAviso(erro.message, "erro");
    }
});


// =====================================================================
// Documentos: escolha do arquivo (clique ou arrastar e soltar)
// =====================================================================

function reservaSelecionada() {
    const id = Number($("reserva-select").value);
    return estado.reservas.find((reserva) => reserva.id === id);
}

function atualizarResumoReserva() {
    const reserva = reservaSelecionada();
    $("reserva-resumo").innerHTML = reserva ? `
        <span>${iconeEspecie(reserva.pet.especie)} <strong>${esc(reserva.pet.nome)}</strong></span>
        <span>${icone("calendario")} Saída em ${formatarData(reserva.dataCheckout)}</span>
        ${selo(reserva.status)}` : "";
    atualizarBotaoProcessar();
}

function atualizarBotaoProcessar() {
    $("botao-processar").disabled = !(reservaSelecionada() && estado.arquivo);
}

function escolherArquivo(arquivo) {
    if (!arquivo) {
        return;
    }
    if (!TIPOS_ACEITOS.includes(arquivo.type)) {
        mostrarAviso("Formato não aceito. Envie um arquivo PDF, PNG ou JPG.", "erro");
        return;
    }
    if (arquivo.size > TAMANHO_MAXIMO) {
        mostrarAviso("Arquivo muito grande. O tamanho máximo é 10MB.", "erro");
        return;
    }
    estado.arquivo = arquivo;
    $("arquivo-nome").textContent = arquivo.name;
    $("arquivo-tamanho").textContent = formatarTamanho(arquivo.size);
    $("arquivo-cartao").hidden = false;
    $("area-arquivo").hidden = true;
    atualizarBotaoProcessar();
}

function removerArquivo() {
    estado.arquivo = null;
    $("arquivo-input").value = "";
    $("arquivo-cartao").hidden = true;
    $("area-arquivo").hidden = false;
    atualizarBotaoProcessar();
}

$("reserva-select").addEventListener("change", atualizarResumoReserva);
$("arquivo-input").addEventListener("change", (evento) => escolherArquivo(evento.target.files[0]));
$("arquivo-remover").addEventListener("click", removerArquivo);
$("botao-cancelar").addEventListener("click", reiniciarEnvio);

// Arrastar e soltar: sem o preventDefault, o navegador abriria o arquivo em vez de entregá-lo à página
const areaArquivo = $("area-arquivo");
["dragenter", "dragover"].forEach((nomeEvento) => {
    areaArquivo.addEventListener(nomeEvento, (evento) => {
        evento.preventDefault();
        areaArquivo.classList.add("arrastando");
    });
});
["dragleave", "drop"].forEach((nomeEvento) => {
    areaArquivo.addEventListener(nomeEvento, (evento) => {
        evento.preventDefault();
        areaArquivo.classList.remove("arrastando");
    });
});
areaArquivo.addEventListener("drop", (evento) => escolherArquivo(evento.dataTransfer.files[0]));


// =====================================================================
// Documentos: stepper e processamento
// =====================================================================

const ETAPAS = document.querySelectorAll("#stepper .etapa");

const ICONES_ETAPA = { concluida: "check", erro: "fechar", alerta: "relogio" };

// Cor da etapa "Resultado": verde (apto), vermelha (não apto) ou amarela (em análise)
const SITUACAO_RESULTADO = { APTO: "concluida", NAO_APTO: "erro", EM_ANALISE_MANUAL: "alerta" };

/** Muda o visual de uma etapa do stepper: "ativa", "concluida", "erro", "alerta" ou "" (pendente). */
function marcarEtapa(indice, situacao, detalhe = "") {
    const etapa = ETAPAS[indice];
    etapa.className = "etapa " + situacao;
    etapa.querySelector(".etapa-bolinha").innerHTML =
        ICONES_ETAPA[situacao] ? icone(ICONES_ETAPA[situacao]) : String(indice + 1);
    etapa.querySelector(".etapa-detalhe").textContent = detalhe;
}

function mostrarProcessando(titulo, texto) {
    $("processando-titulo").textContent = titulo;
    $("processando-texto").textContent = texto;
}

/**
 * Envia o arquivo com XMLHttpRequest (e não fetch) porque ele avisa quando o upload terminou.
 * É isso que permite mudar o stepper de "Upload" para "Processamento" no momento certo.
 */
function enviarDocumento(reservaId, tipo, arquivo, aoTerminarUpload) {
    return new Promise((resolve, reject) => {
        const requisicao = new XMLHttpRequest();
        requisicao.open("POST", `/api/reservas/${reservaId}/documentos`);
        requisicao.responseType = "json";
        requisicao.upload.addEventListener("load", aoTerminarUpload);
        requisicao.addEventListener("load", () => {
            if (requisicao.status >= 200 && requisicao.status < 300) {
                resolve(requisicao.response);
            } else {
                reject(criarErro(requisicao.status, requisicao.response));
            }
        });
        requisicao.addEventListener("error", () => reject(new Error("Não foi possível conectar à API.")));

        const dados = new FormData();
        dados.append("tipo", tipo);
        dados.append("arquivo", arquivo);
        requisicao.send(dados);
    });
}

$("form-documento").addEventListener("submit", async (evento) => {
    evento.preventDefault();
    const reserva = reservaSelecionada();
    const tipo = new FormData(evento.target).get("tipo");
    if (!reserva || !estado.arquivo) {
        return;
    }

    $("form-documento").hidden = true;
    $("processando").hidden = false;
    marcarEtapa(0, "ativa", "Enviando...");
    mostrarProcessando("Enviando o documento...", estado.arquivo.name);

    let etapaAtual = 0;
    try {
        // Etapas 1 e 2: upload, OCR e extração dos dados (uma única chamada à API)
        const documento = await enviarDocumento(reserva.id, tipo, estado.arquivo, () => {
            etapaAtual = 1;
            marcarEtapa(0, "concluida", "Arquivo recebido");
            marcarEtapa(1, "ativa", "Lendo o documento...");
            mostrarProcessando("Lendo a carteira de vacinação...", "OCR e extração dos dados. Pode levar alguns segundos.");
        });
        etapaAtual = 1;
        marcarEtapa(0, "concluida", "Arquivo recebido");

        if (documento.status === "FALHA_PROCESSAMENTO") {
            throw new Error("Não conseguimos processar o documento. Tente outra foto ou um PDF.");
        }
        marcarEtapa(1, "concluida", `OCR ${String(documento.confiancaOcr).replace(".", ",")}% · ${documento.metodoExtracao}`);

        // Etapa 3: validação com as regras
        etapaAtual = 2;
        marcarEtapa(2, "ativa", "Conferindo as regras...");
        mostrarProcessando("Conferindo as vacinas...", "Aplicando as regras de validação.");
        const validacao = await postarJson(`/reservas/${reserva.id}/validacao`, {});
        marcarEtapa(2, "concluida", `${validacao.regrasAtendidas.length} regras atendidas`);

        // Etapa 4: resultado
        marcarEtapa(3, SITUACAO_RESULTADO[validacao.status], ROTULOS_STATUS[validacao.status]);
        mostrarResultado(validacao, reserva.pet.nome);
        await carregarReservas();
    } catch (erro) {
        marcarEtapa(etapaAtual, "erro", "Falhou");
        $("form-documento").hidden = false;
        mostrarAviso(erro.message, "erro");
    } finally {
        $("processando").hidden = true;
    }
});

const TEXTOS_RESULTADO = {
    APTO: {
        icone: "check",
        titulo: (pet) => `Tudo certo! ${pet} está apto para a hospedagem 🐾`,
        texto: "As vacinas estão em dia. A reserva foi confirmada."
    },
    NAO_APTO: {
        icone: "alerta",
        titulo: (pet) => `${pet} ainda não está apto para a hospedagem`,
        texto: "Resolva as pendências abaixo e envie a carteira atualizada."
    },
    EM_ANALISE_MANUAL: {
        icone: "relogio",
        titulo: () => "Documento em análise",
        texto: "Nossa equipe vai conferir a carteira e avisar você em breve."
    }
};

function mostrarResultado(validacao, nomePet) {
    const textos = TEXTOS_RESULTADO[validacao.status];
    const itens = (lista, classe, nomeIcone) => lista.length === 0
        ? `<li class="vazio">Nenhuma</li>`
        : lista.map((item) => `<li class="${classe}">${icone(nomeIcone)}<span>${esc(item)}</span></li>`).join("");

    const resultado = $("resultado");
    resultado.className = "status-" + validacao.status;
    resultado.innerHTML = `
        <div class="cartao">
            <div class="resultado-topo">
                <span class="resultado-icone">${icone(textos.icone)}</span>
                <div>
                    <h2>${esc(textos.titulo(nomePet))}</h2>
                    <p>${textos.texto}</p>
                </div>
            </div>
            <div class="listas-resultado">
                <div>
                    <h3>Regras atendidas</h3>
                    <ul class="lista-icones">${itens(validacao.regrasAtendidas, "ok", "check")}</ul>
                </div>
                <div>
                    <h3>Pendências</h3>
                    <ul class="lista-icones">${itens(validacao.pendencias, "pendente", "alerta")}</ul>
                </div>
            </div>
            <div class="etapas-detalhe">
                ${validacao.etapas.map((etapa) => `<span><strong>${esc(etapa.nome)}:</strong> ${esc(detalheEtapa(etapa))}</span>`).join("")}
            </div>
            <div class="acoes">
                <button type="button" class="botao botao-secundario" id="botao-ver-reservas">Ver reservas</button>
                <button type="button" class="botao botao-primario" id="botao-novo-envio">${icone("envio")} Enviar outro documento</button>
            </div>
        </div>`;
    resultado.hidden = false;

    $("botao-novo-envio").addEventListener("click", reiniciarEnvio);
    $("botao-ver-reservas").addEventListener("click", () => mostrarPagina("reservas"));
}

/** Deixa o detalhe da etapa no jeito brasileiro: "95.3%" → "95,3%" e "NAO_APTO" → "Não apto". */
function detalheEtapa(etapa) {
    if (etapa.nome === "Validação") {
        return ROTULOS_STATUS[etapa.detalhe] || etapa.detalhe;
    }
    return etapa.detalhe.replace(/(\d)\.(\d)/, "$1,$2");
}

/** Volta a tela de envio para o começo (mantém a reserva escolhida). */
function reiniciarEnvio() {
    removerArquivo();
    ETAPAS.forEach((_, indice) => marcarEtapa(indice, indice === 0 ? "ativa" : ""));
    $("resultado").hidden = true;
    $("processando").hidden = true;
    $("form-documento").hidden = false;
}


// =====================================================================
// Resultados: última validação de cada reserva
// =====================================================================

async function carregarResultados() {
    const lista = $("lista-resultados");
    lista.innerHTML = `<p class="vazio">Carregando...</p>`;

    try {
        await carregarReservas();
        // Busca todas as validações ao mesmo tempo. Reserva sem validação responde 404 → null.
        const validacoes = await Promise.all(estado.reservas.map((reserva) =>
            api(`/reservas/${reserva.id}/validacao`).catch(() => null)));

        lista.innerHTML = estado.reservas.length === 0
            ? `<p class="vazio">Nenhuma reserva cadastrada ainda.</p>`
            : estado.reservas.map((reserva, i) => cartaoResultado(reserva, validacoes[i])).join("");
    } catch (erro) {
        lista.innerHTML = "";
        mostrarAviso(erro.message, "erro");
    }
}

function cartaoResultado(reserva, validacao) {
    const cabecalho = `
        <h3><span class="pet-nome">${iconeEspecie(reserva.pet.especie)} ${esc(reserva.pet.nome)}</span>
            ${validacao ? selo(validacao.status) : selo(reserva.status)}</h3>
        <p>Reserva #${reserva.id} · ${formatarData(reserva.dataCheckin)} a ${formatarData(reserva.dataCheckout)}</p>`;

    if (!validacao) {
        return `<div class="cartao resultado-cartao">${cabecalho}<p class="vazio">Ainda não validada.</p></div>`;
    }
    const pendencias = validacao.pendencias.length === 0
        ? `<li class="ok">${icone("check")}<span>Nenhuma pendência</span></li>`
        : validacao.pendencias.map((p) => `<li class="pendente">${icone("alerta")}<span>${esc(p)}</span></li>`).join("");

    return `
        <div class="cartao resultado-cartao status-${esc(validacao.status)}">
            ${cabecalho}
            <p>Validada em ${formatarDataHora(validacao.dataHora)} · ${validacao.regrasAtendidas.length} regras atendidas</p>
            <ul class="lista-icones">${pendencias}</ul>
        </div>`;
}


// =====================================================================
// Início
// =====================================================================

async function iniciar() {
    // Check-in não pode ser no passado: o calendário já começa em hoje
    const hoje = new Date();
    hoje.setMinutes(hoje.getMinutes() - hoje.getTimezoneOffset());
    document.querySelector('#form-reserva [name="dataCheckin"]').min = hoje.toISOString().substring(0, 10);

    try {
        await Promise.all([carregarPets(), carregarReservas()]);
    } catch (erro) {
        mostrarAviso(erro.message, "erro");
    }
}

iniciar();
