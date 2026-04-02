const API_BASE = '/api/v1/weather';
console.log('FRONT VERSION: API_BASE =', API_BASE);

const updateBtn = document.getElementById('updateBtn');
const getBtn = document.getElementById('getBtn');
const cityInput = document.getElementById('cityInput');
const weatherDisplay = document.getElementById('weatherDisplay');

function renderMessage(text) {
    weatherDisplay.innerHTML = `
      <div class="weather-card">
        <div class="weather-loading">${text}</div>
      </div>
    `;
}

function renderWeather(data) {
    weatherDisplay.innerHTML = `
      <div class="weather-card">
        <h2>${data.city}</h2>
        <p class="temp">${data.temperature.toFixed(1)} °C</p>
        <p class="condition">${data.condition}</p>
        <p class="time">${new Date(data.timestamp).toLocaleString()}</p>
      </div>
    `;
}

updateBtn.addEventListener('click', async () => {
    const city = cityInput.value.trim();
    if (!city) {
        renderMessage('Введите город');
        return;
    }

    renderMessage(`Запускаем обновление погоды для ${city}...`);

    try {
        const res = await fetch(`${API_BASE}/${encodeURIComponent(city)}`, {
            method: 'POST'
        });

        if (!res.ok) {
            throw new Error(`HTTP ${res.status}`);
        }

        const data = await res.json();
        renderMessage(`Weather update started for ${data.city}`);
    } catch (e) {
        console.error(e);
        renderMessage('Ошибка при обновлении погоды');
    }
});

getBtn.addEventListener('click', async () => {
    const city = cityInput.value.trim();
    if (!city) {
        renderMessage('Введите город');
        return;
    }

    renderMessage(`Получаем погоду для ${city}...`);

    try {
        const res = await fetch(`${API_BASE}?city=${encodeURIComponent(city)}`);
        if (!res.ok) {
            throw new Error(`HTTP ${res.status}`);
        }

        const data = await res.json();
        renderWeather(data);
    } catch (e) {
        console.error(e);
        renderMessage('Ошибка при получении погоды');
    }
});