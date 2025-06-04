// marine.js

async function fetchMarineInfo(lat, lon, date, time) {
    try {
        const url = `/api/marine/data?lat=${lat}&lon=${lon}&date=${date}&time=${time}`;
        const res = await fetch(url);
        const data = await res.json();

        if (!res.ok) {
            throw new Error("서버 오류: " + data.message);
        }

        renderFishingInfo(data);
    } catch (err) {
        console.error("해양 정보 호출 실패:", err);
    }
}

function renderFishingInfo(data) {
    const container = document.getElementById("marine-info-output");
    if (!container) return;

    container.innerHTML = ""; // 초기화

    // 🐟 갯바위 낚시 지수
    if (data.rock && data.rock.length > 0) {
        const section = document.createElement("div");
        section.innerHTML = `<h3>📍 갯바위 낚시 지수</h3>`;
        data.rock.forEach(item => {
            section.innerHTML += `
                <p><strong>날짜:</strong> ${item.date}</p>
                <p><strong>낚시 지수:</strong> ${item.fishingIndex}</p>
                <p><strong>추천 어종:</strong> ${item.fishType}</p>
                <p><strong>수온:</strong> ${item.waterTemp}</p>
                <hr/>
            `;
        });
        container.appendChild(section);
    }

    // 🛥️ 선상 낚시 지수
    if (data.boat && data.boat.length > 0) {
        const section = document.createElement("div");
        section.innerHTML = `<h3>📍 선상 낚시 지수</h3>`;
        data.boat.forEach(item => {
            section.innerHTML += `
                <p><strong>날짜:</strong> ${item.date}</p>
                <p><strong>낚시 지수:</strong> ${item.fishingIndex}</p>
                <p><strong>추천 어종:</strong> ${item.fishType}</p>
                <p><strong>수온:</strong> ${item.waterTemp}</p>
                <hr/>
            `;
        });
        container.appendChild(section);
    }

    // 🏖️ 일반 여행지 해양 정보
    if (data.tripForecast && data.recommendedTimes) {
        const section = document.createElement("div");
        section.innerHTML = `<h3>📍 일반 해양 정보</h3>`;
        data.recommendedTimes.forEach(item => {
            section.innerHTML += `
                <p><strong>날짜:</strong> ${item.date}</p>
                <p><strong>시간대:</strong> ${item.time}</p>
                <p><strong>기온:</strong> ${item.airTemp}</p>
                <p><strong>풍속:</strong> ${item.windSpeed}</p>
                <p><strong>파고:</strong> ${item.waveHeight}</p>
                <p><strong>수온:</strong> ${item.waterTemp}</p>
                <p><strong>낚시지수:</strong> ${item.fishingScore}점 (${item.fishingIndex})</p>
                <hr/>
            `;
        });
        container.appendChild(section);
    }

    // 🎣 기타 생활낚시 지수
    if (data.etc && data.etc.length > 0) {
        const section = document.createElement("div");
        section.innerHTML = `<h3>📍 기타 생활낚시 지수</h3>`;
        data.etc.forEach(item => {
            section.innerHTML += `
                <p><strong>날짜:</strong> ${item.date}</p>
                <p><strong>설명:</strong> ${item.description}</p>
                <hr/>
            `;
        });
        container.appendChild(section);
    }
}
