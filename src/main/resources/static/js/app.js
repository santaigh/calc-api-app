const OP_CONFIG = {
	add: { label: 'ADD', endpoint: '/api/calc/add', symbol: '+' },
	subtract: { label: 'Subtract', endpoint: '/api/calc/subtract', symbol: '−' },
	multiply: { label: 'Multiply', endpoint: '/api/calc/multiply', symbol: '×' },
	divide: { label: 'Divide', endpoint: '/api/calc/divide', symbol: '÷' },
};

const inputA = document.getElementById('inputA');
const inputB = document.getElementById('inputB');
const submitBtn = document.getElementById('submitBtn');
const resultValue = document.getElementById('resultValue');
const resultTrace = document.getElementById('resultTrace');
const opOptions = document.querySelectorAll('.op-option');

let selectedOp = null;

opOptions.forEach((option) => {
	const radio = option.querySelector('input[type="radio"]');
	radio.addEventListener('change', () => selectOperation(option.dataset.op));
});

function selectOperation(op) {
	selectedOp = op;
	opOptions.forEach((option) => {
		option.classList.toggle('selected', option.dataset.op === op);
	});
	submitBtn.className = 'submit-btn ' + op;
	submitBtn.textContent = OP_CONFIG[op].label;
	submitBtn.disabled = false;
}

submitBtn.addEventListener('click', async () => {
	if (!selectedOp) {
		return;
	}

	const a = Number(inputA.value);
	const b = Number(inputB.value);
	const { endpoint, symbol } = OP_CONFIG[selectedOp];

	submitBtn.disabled = true;
	try {
		const response = await fetch(endpoint, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ a, b }),
		});

		const bodyText = await response.text();

		if (!response.ok) {
			resultValue.textContent = 'Error';
			resultTrace.classList.add('error');
			resultTrace.textContent = `POST ${endpoint} → ${response.status} ${bodyText}`;
			return;
		}

		const c = Number(bodyText);
		resultValue.textContent = `${a} ${symbol} ${b} = ${c}`;
		resultTrace.classList.remove('error');
		resultTrace.textContent = `POST ${endpoint} → ${response.status} ${JSON.stringify({ operation: selectedOp, a, b, c })}`;
	} catch (err) {
		resultValue.textContent = 'Error';
		resultTrace.classList.add('error');
		resultTrace.textContent = `Request failed: ${err.message}`;
	} finally {
		submitBtn.disabled = false;
	}
});
