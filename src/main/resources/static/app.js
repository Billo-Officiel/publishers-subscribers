const DOM = {
    brokerList: document.getElementById('broker-list'),
    pubTopic: document.getElementById('pub-topic'),
    subTopic: document.getElementById('sub-topic'),
    logContainer: document.getElementById('activity-log'),
    formCreate: document.getElementById('form-create'),
    formPublish: document.getElementById('form-publish'),
    formSubscribe: document.getElementById('form-subscribe'),
    formSim: document.getElementById('form-sim'),
    btnPub: document.getElementById('btn-pub'),
    btnSub: document.getElementById('btn-sub')
};

function log(msg, type = 'info') {
    const div = document.createElement('div');
    div.className = `log-item log-${type}`;
    const time = new Date().toLocaleTimeString();
    div.textContent = `[${time}] ${msg}`;
    DOM.logContainer.appendChild(div);
    DOM.logContainer.scrollTop = DOM.logContainer.scrollHeight;
}

let activeTopics = new Set();

async function fetchBrokers() {
    try {
        const res = await fetch('/api/brokers');
        if (!res.ok) throw new Error('Failed to fetch');
        const data = await res.json();
        
        DOM.brokerList.innerHTML = '';
        let currentTopics = new Set(Object.keys(data));
        
        if (currentTopics.size === 0) {
            DOM.brokerList.innerHTML = '<div class="broker-item" style="color:var(--text-secondary)">Aucun sujet actif.</div>';
        }

        for (const [topic, info] of Object.entries(data)) {
            const row = document.createElement('div');
            row.className = 'broker-item';
            
            const percentage = Math.round((info.count / info.capacity) * 100);
            const colorClass = percentage >= 100 ? 'bg-red' : (percentage > 70 ? 'bg-yellow' : 'bg-blue');
            
            row.innerHTML = `
                <div class="broker-info">
                    <span class="broker-name">${topic}</span>
                    <span class="broker-count">${info.count} / ${info.capacity}</span>
                </div>
                <div class="progress-bar">
                    <div class="progress-fill ${colorClass}" style="width: ${percentage}%"></div>
                </div>
            `;
            DOM.brokerList.appendChild(row);
            
            if (!activeTopics.has(topic)) {
                activeTopics.add(topic);
                DOM.pubTopic.add(new Option(topic, topic));
                DOM.subTopic.add(new Option(topic, topic));
            }
        }
    } catch (e) {
        // silent fail for polling
    }
}

DOM.formCreate.addEventListener('submit', async (e) => {
    e.preventDefault();
    const topic = document.getElementById('create-topic').value.trim();
    const capacity = document.getElementById('create-capacity').value;
    if (!topic || !capacity) return;
    
    try {
        const res = await fetch(`/api/brokers/${topic}`, {
            method: 'POST',
            headers: {'Content-Type':'application/json'},
            body: JSON.stringify({capacity: parseInt(capacity)})
        });
        const text = await res.text();
        if (res.ok) {
            log(text, 'success');
            document.getElementById('create-topic').value = '';
            fetchBrokers();
        } else {
            log(`Failed to create: ${text}`, 'error');
        }
    } catch (err) {
        log(`Error: ${err.message}`, 'error');
    }
});

DOM.formPublish.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (DOM.btnPub.disabled) return;
    
    const topic = DOM.pubTopic.value;
    const pubName = document.getElementById('pub-name').value.trim();
    if (!topic || !pubName) return;
    
    DOM.btnPub.innerHTML = `<svg class="spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12a9 9 0 1 1-6.219-8.56"></path></svg>`;
    DOM.btnPub.disabled = true;

    try {
        const res = await fetch(`/api/brokers/${topic}/publish`, {
            method: 'POST',
            headers: {'Content-Type':'application/json'},
            body: JSON.stringify({publisherName: pubName})
        });
        const text = await res.text();
        if (res.ok) log(text, 'success');
        else log(`${text}`, 'error');
    } catch (err) {
       log(`Error: ${err.message}`, 'error');
    } finally {
        DOM.btnPub.innerHTML = 'Publier';
        DOM.btnPub.disabled = false;
        fetchBrokers();
    }
});

DOM.formSubscribe.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (DOM.btnSub.disabled) return;

    const topic = DOM.subTopic.value;
    const subName = document.getElementById('sub-name').value.trim();
    if (!topic || !subName) return;
    
    DOM.btnSub.innerHTML = `<svg class="spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12a9 9 0 1 1-6.219-8.56"></path></svg>`;
    DOM.btnSub.disabled = true;

    try {
        const res = await fetch(`/api/brokers/${topic}/subscribe?subscriberName=${subName}`);
        const text = await res.text();
        if (res.ok) log(text, 'success');
        else log(`${text}`, 'error');
    } catch (err) {
       log(`Error: ${err.message}`, 'error');
    } finally {
        DOM.btnSub.innerHTML = 'Consommer';
        DOM.btnSub.disabled = false;
        fetchBrokers();
    }
});

DOM.formSim.addEventListener('submit', async (e) => {
    e.preventDefault();
    const n = document.getElementById('sim-n').value;
    const p = document.getElementById('sim-p').value;
    const s = document.getElementById('sim-s').value;
    
    try {
        const res = await fetch('/api/simulation/start', {
            method: 'POST',
            headers: {'Content-Type':'application/json'},
            body: JSON.stringify({n: parseInt(n), p: parseInt(p), s: parseInt(s), t: 100})
        });
        const text = await res.text();
        log(text, 'info');
    } catch (err) {
        log(`Error: ${err.message}`, 'error');
    }
});

// Initial boot
log('Interface de la console initialisée.', 'info');
fetchBrokers();
setInterval(fetchBrokers, 1000);
