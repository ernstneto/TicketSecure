"""
Script de Treinamento do Modelo Antifraude (Random Forest)
"""
import pandas as pd
import random
from sklearn.ensemble import RandomForestClassifier
import pickle

print("[⚙️] Iniciando simulação de dados históricos...")

# 1. GERANDO DADOS DE MENTIRA (Nosso "banco de dados" de treino)
# Vamos criar 1000 transações passadas para a IA estudar
dados = []
for _ in range(1000):
    # Caracteristicas (Features) da transacao
    valor_total = round(random.uniform(50.0, 3000.0), 2)
    hora_tentativa = random.randint(0,23)

    # Simulando um "Score de Risco do IP" (0 = Seguro, 100 = Hacker/Botnet)
    ip_risk_score = random.randint(0,100)

    # 2. Definindo As Regras do Golpe (O que AI tem que aprender a enxergar)
    # Se for de horario suspeito, com valor muito alto, e um IP suspeito = FRAUDE (1)
    is_fraude = 0
    if hora_tentativa < 6 and valor_total > 1500 and ip_risk_score > 70:
        is_fraude = 1
    elif ip_risk_score > 90: # IP super perigoso tambem é fraude
        is_fraude = 1
    elif valor_total > 2500 and hora_tentativa > 22:
        is_fraude = 1
    
    dados.append([valor_total, hora_tentativa, ip_risk_score, is_fraude])

# Cria a tabela estilo excel
df = pd.DataFrame(dados, columns=['valor_total', 'hora_tentativa', 'ip_risk_score', 'is_fraude'])

print(f"[📊] Dados gerados! Total de fraudes simuladas: {df['is_fraude'].sum()} de 1000")

# 3. Separando o que é pergunta do que é resposta
x = df[['valor_total', 'hora_tentativa', 'ip_risk_score']] # as perguntas (Features)
y = df['is_fraude'] # as respostas (Target)

# 4. Treinando a inteligencia artificial
print("[🧠] Treinando o modelo Random Forest...")
modelo = RandomForestClassifier(n_estimators=100, random_state=42)
modelo.fit(x, y)

# 5. Salvando o cerebro no disco
with open('modelo_antifraude.pkl', 'wb') as arquivo_modelo:
    pickle.dump(modelo, arquivo_modelo)

print("[✅] Treinamento concluído! Modelo salvo como 'modelo_antifraude.pkl'")
