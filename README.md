# Barbearia do Recruta Zero 🪖✂️

Trabalho Prático II — Sistemas Operacionais  
Universidade Federal do Amazonas — Instituto de Computação  

## 📄 Descrição

Simulação do clássico problema do **"Barbeiro Dorminhoco"**, adaptado para a **Barbearia do Recruta Zero**, utilizando **threads e semáforos (pthreads)**.

O objetivo é gerenciar o atendimento de uma barbearia militar, priorizando filas de clientes (oficiais, sargentos e cabos) com base em regras de prioridade e geração de relatórios de desempenho.

## 🏗️ Funcionamento

- 🪑 **20 cadeiras disponíveis**, distribuídas em **3 filas FIFO**:
  - Oficiais (prioridade mais alta)
  - Sargentos
  - Cabos (prioridade mais baixa)

- ✂️ **O barbeiro (Recruta Zero)** atende um cliente por vez, priorizando:
  1. Oficiais
  2. Sargentos
  3. Cabos

- 😴 **Sargento Tainha** adiciona novos clientes periodicamente (1 a 5 segundos), se houver espaço.

- 📝 **Tenente Escovinha** coleta dados a cada 3 segundos e gera um relatório final.

## 🧠 Relatório Gerado

- Estado de ocupação das cadeiras (percentual por categoria e livres)
- Comprimento médio das filas
- Tempo médio de atendimento por categoria
- Tempo médio de espera por categoria
- Número de atendimentos por categoria
- Número total de clientes por categoria

## ▶️ Casos Simulados

- **Caso A:** 1 barbeiro atende todas as filas, obedecendo a prioridade.  
- **Caso B:** 2 barbeiros atendem, ainda respeitando a prioridade.  
- **Caso C:** 3 barbeiros, cada um dedicado a uma fila, mas podem ajudar nas outras se estiverem vazias, seguindo a ordem de prioridade.

## 🔧 Tecnologias

- Linguagem JAVA;
