// marine.js

async function fetchMarineInfo(lat, lon, departureDate, arrivalTime) {
    try {
        // Spring Boot에서 /api/marine으로 GET 매핑(Controller 참고)
        const url = `/api/marine?lat=${lat}&lon=${lon}&departureDate=${encodeURIComponent(departureDate)}&arrivalTime=${encodeURIComponent(arrivalTime)}`;
        const res = await fetch(url);
        if (!res.ok) {
            const data = await res.json().catch(() => ({}));
            throw new Error("서버 오류: " + (data.message || res.statusText));
        }
        const data = await res.json();
        renderFishingInfo(data);
    } catch (err) {
        console.error("marine.js: 해양 정보 호출 실패:", err);
        alert("해양 정보를 불러올 수 없습니다.");
    }
}

function renderFishingInfo(data) {
   const container = document.getElementById("marine-info-output");
   if (!container) return;
   container.innerHTML = "";

   // 갯바위 낚시 지수
   if (data.rock && data.rock.length > 0) {
     const section = document.createElement("div");
     section.innerHTML = `<h3>📍 갯바위 낚시 지수</h3>`;
     data.rock.forEach(item => {
       section.innerHTML += `
         <p><strong>날짜:</strong> ${item.dateStr}</p>
         <p><strong>낚시 지수:</strong> ${item.fishingIndex}</p>
         <p><strong>추천 어종:</strong> ${item.fishType}</p>
         <p><strong>수온:</strong> ${item.waterTemp}</p>
         <hr/>
       `;
     });
     container.appendChild(section);
   }

   // 선상 낚시 지수
   if (data.boat && data.boat.length > 0) {
     const section = document.createElement("div");
     section.innerHTML = `<h3>🛥️ 선상 낚시 지수</h3>`;
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
   if (data.recommendedTimes && data.recommendedTimes.length > 0) {
       const section = document.createElement("div");
       section.innerHTML = `<h3>🏖️ 일반 해양 예보</h3>`;
       data.recommendedTimes.forEach(item => {
           section.innerHTML += `
               <p><strong>날짜:</strong> ${item.date}</p>
               <p><strong>시간대:</strong> ${item.time}</p>
               <p><strong>수온:</strong> ${item.waterTemp ?? '-'}℃</p>
               <p><strong>파고:</strong> ${item.waveHeight ?? '-'}m</p>
               <p><strong>풍속:</strong> ${item.windSpeed ?? '-'} m/s</p>
               <p><strong>기온:</strong> ${item.airTemp ?? '-'}℃</p>
               <p><strong>어종:</strong> ${item.fishType ?? '-'}</p>
               <hr/>
           `;
       });
       container.appendChild(section);
   }

   // 기타 생활낚시 지수
   if (data.etc && data.etc.length > 0) {
     const section = document.createElement("div");
     section.innerHTML = `<h3>🎣 기타 생활낚시 지수</h3>`;
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
