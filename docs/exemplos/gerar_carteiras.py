"""
Gera carteiras de vacinação FICTÍCIAS para demonstrar os resultados da validação.

Uso (na raiz do projeto):  python3 docs/exemplos/gerar_carteiras.py
Requer: Pillow (pip install pillow) e a fonte DejaVu (padrão no Ubuntu).
"""
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

PASTA = Path(__file__).parent
FONTES = Path("/usr/share/fonts/truetype/dejavu")
LARGURA, ALTURA = 1654, 2339  # A4 em 200 DPI


def fonte(tamanho, negrito=False):
    nome = "DejaVuSans-Bold.ttf" if negrito else "DejaVuSans.ttf"
    return ImageFont.truetype(str(FONTES / nome), tamanho)


def gerar(arquivo, nome_pet, vacinas):
    img = Image.new("RGB", (LARGURA, ALTURA), "white")
    d = ImageDraw.Draw(img)
    centro = LARGURA // 2

    d.text((centro, 120), "CARTEIRA DE VACINAÇÃO", font=fonte(64, True), fill="black", anchor="mm")
    d.text((centro, 200), "Clínica Veterinária Patas Felizes (fictícia)", font=fonte(32), fill="black", anchor="mm")
    d.line((100, 290, 1554, 290), fill="black", width=4)

    dados = [
        ("Nome do pet:", nome_pet),
        ("Espécie:", "Canina"),
        ("Raça:", "Golden Retriever"),
        ("Data de nascimento:", "15/03/2022"),
        ("Nome do tutor:", "Caio Oliveira"),
    ]
    y = 345
    for rotulo, valor in dados:
        d.text((120, y), rotulo, font=fonte(40, True), fill="black")
        d.text((640, y), valor, font=fonte(40), fill="black")
        y += 75
    d.line((100, 745, 1554, 745), fill="black", width=4)

    d.text((120, 800), "VACINAS APLICADAS", font=fonte(46, True), fill="black")
    colunas = [120, 560, 900, 1240]
    for x, titulo in zip(colunas, ["Vacina", "Aplicação", "Próxima dose", "Veterinário"]):
        d.text((x, 885), titulo, font=fonte(34, True), fill="black")
    d.line((100, 945, 1554, 945), fill="black", width=2)

    y = 975
    for vacina in vacinas:
        for x, valor in zip(colunas, [*vacina, "CRMV-PE 12345"]):
            d.text((x, y), valor, font=fonte(32), fill="black")
        y += 80
    d.line((100, 1175, 1554, 1175), fill="black", width=2)

    rodape = ["Médica veterinária responsável: Dra. Ana Souza", "CRMV-PE 12345", "Recife, 15/04/2026"]
    y = 1235
    for linha in rodape:
        d.text((120, y), linha, font=fonte(38), fill="black")
        y += 65

    d.text((centro, 2210), "DOCUMENTO FICTÍCIO - USO EXCLUSIVO PARA TESTES DO PROJETO PETCHECK-IN",
           font=fonte(26, True), fill="black", anchor="mm")

    img.save(PASTA / arquivo)
    print("gerado:", PASTA / arquivo)


V10_EM_DIA = ("V10 (Polivalente)", "15/04/2026", "15/04/2027")

# NÃO APTO (R3 e R5): antirrábica aplicada há mais de 12 meses
gerar("carteira_thor_vencida.png", "Thor",
      [("Antirrábica", "10/02/2025", "10/02/2026"), V10_EM_DIA])

# NÃO APTO (R5): antirrábica vence em 02/12/2026, no meio da estadia (check-out 05/12/2026)
gerar("carteira_thor_vence_na_estadia.png", "Thor",
      [("Antirrábica", "02/12/2025", "02/12/2026"), V10_EM_DIA])

# EM ANÁLISE MANUAL (R9): vacinas em dia, mas o nome no documento é um apelido
gerar("carteira_thor_nome_diferente.png", "Thorzinho",
      [("Antirrábica", "10/03/2026", "10/03/2027"), V10_EM_DIA])
