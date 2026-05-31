"""
Microsserviço de detecção de fraudes (Consumer RabbitMQ).
Lê a fila de verificação e envia o resultado de volta para o Java.
"""
from datetime import datetime
import json
import pika
import pandas as pd
import pickle
import sys
import requests
import time

print("[🧠] Carregando modelo de Inteligência Artificial...")

try:
    with open('modelo_antifraude.pkl', 'rb') as arquivo_modelo:
        modelo_antifraude = pickle.load(arquivo_modelo)
    print("[✅] Cérebro online e pronto para operar!")
except FileNotFoundError:
    print("[❌] O arquivo 'modelo_antifraude.pkl' não foi encontrado. Rode o treinador.py primeiro!")
    sys.exit(1)

def callback(ch, method, _properties, body):
    """Processa as mensagens recebidas da fila do RabbitMQ."""
    print("\n[🚨 ALERTA] Novo dossiê recebido na catraca!")

    try:
        dossie = json.loads(body)
        print(f"-> Analisando Reserva ID: {dossie.get('reserveId')}")
        #print(f"-> Valor Total: R$ {dossie.get('totalAmount')}")
        #print(f"-> IP de Origem: {dossie.get('sourceIp')}")
        #valor=dossie.get('totalAmount',250.0)
        valor = dossie.get('totalAmount', dossie.get('price', 250.0))
        if isinstance(valor, str):
            valor = float(valor)
        print(f"-> valor do ingresso: R$ {float(valor):.2f}")

        ip_cliente = dossie.get('sourceIp', '5.9.144.226')
        print(f"-> IP de Origem: {ip_cliente}")

        time.sleep(2)

        hora = 23
        timestamp_str = dossie.get('attemptTime', dossie.get('timestamp', ''))
        if timestamp_str:
            try:
                dt_obj = datetime.fromisoformat(timestamp_str.replace('Z', '+00:00'))
                hora = dt_obj.hour
            except ValueError:
                print(f"[💥 Erro] Timestamp em formato inválido: {timestamp_str}")

        print("[⏳] Processando rede neural antifraude...")    
        print("[🌐] Consultando reputação do IP via radar global...")
        
        # Bate na API gratuita pedindo o País, se é Proxy e se é Hosting (Data Center)
        url = f"http://ip-api.com/json/{ip_cliente}?fields=country,proxy,hosting"
        resposta_api = requests.get(url, timeout=3).json()
        
        ip_risk = 10 # Risco base baixo
        
        pais = resposta_api.get('country', 'Desconhecido')
        is_proxy = resposta_api.get('proxy', False)
        is_hosting = resposta_api.get('hosting', False)

        # Regras Comportamentais L7 (Firewall de Aplicação)
        if is_proxy or is_hosting:
            print(f"[⚠️ WARNING] IP {ip_cliente} mascarado! (VPN/Data Center)")
            ip_risk = 99 # Risco máximo, cambista tentando se esconder!
        elif pais not in ['Brazil']: # Se o evento for focado no BR
            print(f"[⚠️ WARNING] Tentativa de compra internacional: {pais}")
            ip_risk += 40

        print("-" * 40)
        print("🔍 DADOS DA TRANSAÇÃO:")
        print(f"Reserva ID: {dossie.get('reserveId')}")
        print(f"Origem: {pais} (IP: {ip_cliente})")
        print(f"Score de Risco Calculado: {ip_risk}/100")
        print("-" * 40)

        print("[⏳] IA analisando os padrões de risco...")

        # Prepara a pergunta para a Inteligência Artificial
        dados_pergunta = pd.DataFrame([[valor, hora, ip_risk]], 
                                      columns=['valor_total', 'hora_tentativa', 'ip_risk_score'])
        
        previsao = modelo_antifraude.predict(dados_pergunta)[0]

        if previsao == 1:
            status_fraude = "DENIED"
            print("[❌] Decisão da IA: BLOQUEADO (Padrão de Fraude Detectado)")
        else:
            status_fraude = "APPROVED"
            print("[✅] Decisão da IA: APPROVED")

        # --- ENVIANDO O VEREDITO DE VOLTA PARA O JAVA ---
        resposta = {
            "reserveId": dossie.get('reserveId'),
            "status": status_fraude
        }
        
        propriedades = pika.BasicProperties(
            content_type='application/json',
            headers={'__TypeId__': 'com.ticketsecure.dto.FraudResultDTO'}
        )

        ch.basic_publish(
            exchange='',
            routing_key='ticketsecure.fraud.result.queue',
            body=json.dumps(resposta),
            properties=propriedades
        )

        ch.basic_ack(delivery_tag=method.delivery_tag)
        print("-" * 40 + "\n")

    except json.JSONDecodeError as e:
        print(f"[💥 Erro] JSON inválido recebido: {e}")
    except requests.RequestException as e:
        print(f"[💥 Erro] Falha na consulta externa (IP API): {e}")
    except (KeyError, ValueError) as e:
        print(f"[💥 Erro] Dados do dossiê inválidos ou incompletos: {e}")
    except (pika.exceptions.AMQPError, AttributeError, TypeError, IndexError, OSError) as e:
        # Tratamento para erros esperados/operacionais mais comuns sem capturar
        # a Exception base, evitando o warning de "broad-exception-caught".
        print(f"[💥 Erro] Falha ao processar dossiê: {e}")

def iniciar_cerebro():
    # Inicializa a conexão com o RabbitMQ e começa a escutar a fila.
    print("[🔌] Conectando ao RabbitMQ...")

    credenciais = pika.PlainCredentials('guest', 'guest')
    parametros = pika.ConnectionParameters(
        host='127.0.0.1',
        port=5672,
        virtual_host='/',
        credentials=credenciais
    )
    connection = pika.BlockingConnection(parametros)
    channel = connection.channel()

    fila_entrada = 'ticketsecure.fraud.check.queue'
    channel.queue_declare(queue=fila_entrada, durable=True)

    channel.basic_consume(queue=fila_entrada, on_message_callback=callback)

    print(f"[*] Operação Normal. Escutando a fila '{fila_entrada}'. CTRL+C para parar.")

    try:
        channel.start_consuming()
    except KeyboardInterrupt:
        print("\n[🛑] Interrupção solicitada pelo usuário (CTRL+C).")
        print("[🔌] Desligando o cérebro IA e fechando conexões com o RabbitMQ...")
        
        # Para de escutar a fila e fecha a conexão com o servidor com segurança
        channel.stop_consuming()
        connection.close()
        
        print("[💤] Microsserviço encerrado com sucesso. Até logo!\n")
        sys.exit(0)


if __name__ == '__main__':
    iniciar_cerebro()