const messages = document.getElementById('messages');
const emptyHint = document.getElementById('emptyHint');
const chatForm = document.getElementById('chatForm');
const chatInput = document.getElementById('chatInput');
const sendBtn = document.getElementById('sendBtn');

function addMessage(text, cls) {
	if (emptyHint && emptyHint.isConnected) {
		emptyHint.remove();
	}
	const el = document.createElement('div');
	el.className = 'msg ' + cls;
	el.textContent = text;
	messages.appendChild(el);
	messages.scrollTop = messages.scrollHeight;
	return el;
}

function addTrace(parentEl, text) {
	const trace = document.createElement('div');
	trace.className = 'msg-trace';
	trace.textContent = text;
	parentEl.appendChild(trace);
	messages.scrollTop = messages.scrollHeight;
}

chatForm.addEventListener('submit', async (event) => {
	event.preventDefault();
	const text = chatInput.value.trim();
	if (!text) {
		return;
	}

	addMessage(text, 'user');
	chatInput.value = '';
	sendBtn.disabled = true;

	try {
		const response = await fetch('/api/chat', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ message: text }),
		});

		const bodyText = await response.text();

		if (!response.ok) {
			const bot = addMessage(bodyText || 'Something went wrong.', 'bot error');
			addTrace(bot, `POST /api/chat → ${response.status}`);
			return;
		}

		const data = JSON.parse(bodyText);
		const bot = addMessage(data.reply, 'bot');
		addTrace(bot, `POST /api/chat → ${response.status} ${JSON.stringify(data)}`);
	} catch (err) {
		addMessage('Request failed: ' + err.message, 'bot error');
	} finally {
		sendBtn.disabled = false;
		chatInput.focus();
	}
});
