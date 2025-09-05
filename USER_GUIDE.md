## Guia do Usuário — Sistema de Chamados via WhatsApp (transrv)

### O que é
O sistema acompanha mensagens em grupos do WhatsApp e, quando identifica um assunto relevante (via IA), abre um chamado automaticamente, notifica as pessoas responsáveis e mostra tudo em um painel em tempo real (dashboard).

### O que muda para você
- **Abertura automática (em evolução)**: a IA tenta identificar o tema e abrir o chamado. Porém, continue marcando a equipe normalmente, pois a IA pode falhar ou não reconhecer corretamente em alguns casos.
- **Notificações imediatas**: os responsáveis são alertados assim que o chamado é aberto, a qualquer hora.
- **Fechamento simples**: basta responder citando a mensagem de abertura ou informar o ID do chamado na mensagem.
- **Transparência**: o dashboard mostra a fila de chamados, status e últimos eventos em tempo real.

### Como os chamados são abertos
1. Você envia uma mensagem no grupo.
2. O sistema processa o conteúdo (texto, áudio, imagem ou documento):
   - **Texto**: lido diretamente.
   - **Áudio**: transcrito automaticamente.
   - **Imagem**: geramos uma breve descrição do conteúdo.
   - **Documento (PDF/Word/Excel/etc.)**: o texto é extraído e resumido.
3. Uma **IA** classifica se aquilo é motivo para abrir chamado e determina o tipo (termo de alerta).
4. Se sim, o chamado é aberto e o primeiro nível de responsáveis é notificado.

Observação: mensagens de conversa geral que a IA entende que não demandam ação não abrem chamado.

### Notificações e escalonamento
- Ao abrir, o sistema alerta o **Nível 1** imediatamente.
- Escalonamentos automáticos (a partir da abertura):
  - **Nível 2** após ~X minuto
  - **Nível 3** após ~X minutos
- Se ninguém fechar o chamado, ele **expira em X hora** como “Fechado sem solução”.

### Como fechar um chamado
Você tem duas formas fáceis:

1) Responder citando a mensagem de abertura do chamado no WhatsApp.
- Basta responder diretamente à mensagem que abriu o chamado (resposta encadeada). O sistema reconhece e fecha.

2) Informar o ID do chamado na mensagem.
- Escreva algo com o ID no próprio texto, por exemplo:
  - "ID: 123"
  - "id do chamado: 123"
  - "Id 123" (variações de maiúsculas/minúsculas também funcionam)

Se o chamado estiver aberto, ele será fechado e registrado quem fechou e qual mensagem motivou o fechamento.

### Dashboard (painel)
- Acesse o painel em: `/dashboard` do sistema.
- Você verá:
  - **Abertos, Fechados, Fechados s/ solução, Abertos hoje**
  - **Últimos 20 chamados** com: ID, grupo, tipo (alerta), status, nível atual, horários de abertura/fechamento.
- O painel atualiza **em tempo real**; não precisa recarregar a página.

### Boas práticas ao enviar mensagens
- Seja claro e direto sobre o problema.
- Em áudios, fale próximo ao microfone e de forma objetiva.
- Em imagens, se possível adicione legenda curta para contexto.
- Para fechar, prefira **responder citando** a mensagem do chamado; é mais à prova de erro.

### Limitações e regras importantes
- Nem toda mensagem abre chamado: a IA prioriza mensagens com termos de alerta relevantes.
- A IA pode errar na classificação ou deixar de abrir um chamado mesmo quando cabível; por isso, continue marcando manualmente a equipe.
- Documentos muito grandes podem ser resumidos; detalhes finos podem não aparecer.
- Expiração automática em 1 hora se ninguém fechar.

### Melhoria contínua
- Estamos melhorando continuamente o modelo e as regras. Comportamentos indesejados podem ocorrer no início e serão ajustados ao longo do tempo com base no uso e feedback.

### Privacidade e uso de dados
- Para classificar, transcrever e descrever conteúdo, partes das mensagens (texto/áudio/imagem/documento) podem ser processadas por serviços de IA.
- O sistema registra informações mínimas para auditoria (por exemplo: quem abriu/fechou, horários e IDs de mensagens).

### Dúvidas e suporte
- Se achar que um chamado deveria ter sido aberto e não foi, reenvie a mensagem com mais contexto (ou peça ajuda no grupo responsável).
- Se o dashboard não carregar, tente novamente após alguns segundos.
- Para suporte, contate a equipe responsável pelo sistema.


