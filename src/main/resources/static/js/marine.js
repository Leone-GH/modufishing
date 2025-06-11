// ==== 유틸 ====
// 그룹핑 (rock/boat 낚시 지수/어종 합치기)
function groupFishingDataByDateAndTime(arr) {
  const grouped = {};
  arr.forEach(item => {
    const key = `${item.dateStr}|${item.timeStr}|${item.fishingIndex}|${item.waterTemp}`;
    if (!grouped[key]) {
      grouped[key] = {
        dateStr: item.dateStr,
        timeStr: item.timeStr,
        fishingIndex: item.fishingIndex,
        waterTemp: item.waterTemp,
        fishNames: []
      };
    }
    grouped[key].fishNames.push(item.fishName);
  });
  return Object.values(grouped);
}
function isMorning(timeStr) {
  const [h, m] = timeStr.split(":").map(Number);
  return h < 12 || (h === 12 && m === 0);
}
function isAfternoon(timeStr) {
  const [h, m] = timeStr.split(":").map(Number);
  return h > 12 || (h === 12 && m > 0);
}

// ==== 모달 전체보기(어종+지수도 표시) ====
function showMarineInfoModal(data) {
  let modal = document.getElementById("marine-modal");
  if (!modal) {
    modal = document.createElement("div");
    modal.id = "marine-modal";
    modal.style = "display:block; position:fixed; left:0;top:0;width:100vw;height:100vh;z-index:9999;background:rgba(0,0,0,0.6);overflow:auto;";
    document.body.appendChild(modal);
  } else {
    modal.style.display = "block";
  }
  let html = `<div style="margin:40px auto; max-width:600px; background:#fff; border-radius:8px; padding:24px; box-shadow:0 2px 10px #0003; position:relative;">
    <button id="marine-modal-close" style="position:absolute;right:15px;top:10px;">닫기</button>
    <h2>전체 해양예보(1주일)</h2>
    <div style="max-height:70vh;overflow:auto;">`;

  let days = Array.from(new Set((data.recommendedTimes || []).map(i => i.date)));
  days.forEach(date => {
    html += `<h4 style="margin-top:1em;">📅 ${date}</h4>`;
    let dayArr = data.recommendedTimes.filter(i => i.date === date);
    dayArr.forEach(i => {
      // 해당 날짜+시간대의 갯바위 낚시지수/어종
      let fishNames = [];
      let fishingIndexes = [];
      if (data.rock && Array.isArray(data.rock)) {
        data.rock
          .filter(r => r.dateStr === i.date && r.timeStr === i.time)
          .forEach(r => {
            if (r.fishName) fishNames.push(r.fishName);
            if (r.fishingIndex) fishingIndexes.push(r.fishingIndex);
          });
        fishNames = [...new Set(fishNames)];
        fishingIndexes = [...new Set(fishingIndexes)];
      }
      html += `
        <div style="border:1.5px solid #dde; border-radius:6px; padding:10px; margin-bottom:10px; background:#f7fafd;">
          <p><strong>시간대:</strong> ${i.time}</p>
          <p><strong>날씨:</strong> ${i.weather ?? i.sky ?? i.description ?? '-'}</p>
          <p><strong>수온:</strong> ${i.waterTemp ?? '-'}℃, <strong>파고:</strong> ${i.waveHeight ?? '-'}m</p>
          <p><strong>풍속:</strong> ${i.windSpeed ?? '-'} m/s, <strong>기온:</strong> ${i.airTemp ?? '-'}℃</p>
          <p><strong>낚시 지수:</strong> ${fishingIndexes.length > 0 ? fishingIndexes.join(", ") : "-"}</p>
          <p><strong>추천 어종:</strong> ${fishNames.length > 0 ? fishNames.join(", ") : "-"}</p>
        </div>`;
    });
  });
  html += "</div></div>";
  modal.innerHTML = html;
  document.getElementById("marine-modal-close").onclick = () => {
    modal.style.display = "none";
  };
}

// === 반드시 window에 export ===
window.renderFishingInfo = function (data, targetDate, arrivalTime, triptype) {
  const c = document.getElementById("marine-info-output");
  if (!c) return;
  c.innerHTML = "";

  if (!data) {
    c.innerHTML = "<p>해양 정보가 없습니다.</p>";
    return;
  }
  let [h, m] = arrivalTime.split(":").map(Number);
  const highlightPeriod = (h < 12 || (h === 12 && m === 0)) ? "오전" : "오후";

  let label, target;
  if (triptype === "rock") {
    label = "갯바위/방파제"; target = data.rock || [];
  } else if (triptype === "boat") {
    label = "선상"; target = data.boat || [];
  } else { label = ""; target = []; }

  // 오전
  const morning = groupFishingDataByDateAndTime(target.filter(i => i.dateStr === targetDate && isMorning(i.timeStr)));
  if (morning.length) {
    let section = `<h3>☀️ <span${highlightPeriod === "오전" ? ' style="background:yellow;"' : ''}>오전</span> ${label} 낚시 지수</h3>`;
    morning.forEach(i => {
      const hi = (i.timeStr === arrivalTime && highlightPeriod === "오전");
      section += `<div${hi ? ' style="border:2px solid orange; background:#fff3cd;"' : ''}>
        <p><strong>날짜:</strong> ${i.dateStr}</p>
        <p><strong>시간대:</strong> ${i.timeStr}</p>
        <p><strong>낚시 지수:</strong> ${i.fishingIndex ?? "-"}</p>
        ${triptype === "rock" ? `<p><strong>추천 어종:</strong> ${i.fishNames.join(", ")}</p>` : ""}
        <p><strong>수온:</strong> ${i.waterTemp}</p></div><hr/>`;
    });
    c.innerHTML += section;
  }
  // 오후
  const afternoon = groupFishingDataByDateAndTime(target.filter(i => i.dateStr === targetDate && isAfternoon(i.timeStr)));
  if (afternoon.length) {
    let section = `<h3>🌇 <span${highlightPeriod === "오후" ? ' style="background:yellow;"' : ''}>오후</span> ${label} 낚시 지수</h3>`;
    afternoon.forEach(i => {
      const hi = (i.timeStr === arrivalTime && highlightPeriod === "오후");
      section += `<div${hi ? ' style="border:2px solid orange; background:#fff3cd;"' : ''}>
        <p><strong>날짜:</strong> ${i.dateStr}</p>
        <p><strong>시간대:</strong> ${i.timeStr}</p>
        <p><strong>낚시 지수:</strong> ${i.fishingIndex ?? "-"}</p>
        ${triptype === "rock" ? `<p><strong>추천 어종:</strong> ${i.fishNames.join(", ")}</p>` : ""}
        <p><strong>수온:</strong> ${i.waterTemp}</p></div><hr/>`;
    });
    c.innerHTML += section;
  }
  // 해당일 해양예보만
  if (data.recommendedTimes) {
    let section = `<h3>🏖️ 일반 해양 예보</h3>`;
    data.recommendedTimes.filter(i => i.date === targetDate).forEach(i => {
      // 해당 날짜+시간대의 갯바위 낚시지수/어종
      let fishNames = [];
      let fishingIndexes = [];
      if (data.rock && Array.isArray(data.rock)) {
        data.rock
          .filter(r => r.dateStr === i.date && r.timeStr === i.time)
          .forEach(r => {
            if (r.fishName) fishNames.push(r.fishName);
            if (r.fishingIndex) fishingIndexes.push(r.fishingIndex);
          });
        fishNames = [...new Set(fishNames)];
        fishingIndexes = [...new Set(fishingIndexes)];
      }
      section += `
        <p><strong>날짜:</strong> ${i.date}</p>
        <p><strong>날씨:</strong> ${i.weather ?? i.sky ?? i.description ?? '-'}</p>
        <p><strong>시간대:</strong> ${i.time}</p>
        <p><strong>수온:</strong> ${i.waterTemp ?? '-'}℃</p>
        <p><strong>파고:</strong> ${i.waveHeight ?? '-'}m</p>
        <p><strong>풍속:</strong> ${i.windSpeed ?? '-'} m/s</p>
        <p><strong>기온:</strong> ${i.airTemp ?? '-'}℃</p>
        <p><strong>낚시 지수:</strong> ${fishingIndexes.length > 0 ? fishingIndexes.join(", ") : "-"}</p>
        <p><strong>추천 어종:</strong> ${fishNames.length > 0 ? fishNames.join(", ") : "-"}</p>
        <hr/>`;
    });
    c.innerHTML += section;
  }
  // 전체보기 버튼
  if (data.recommendedTimes && data.recommendedTimes.length > 1) {
    c.innerHTML += `<button id="show-all-marine-info-btn" style="margin-top:10px;">전체 해양예보 보기(1주일)</button>`;
    setTimeout(() => {
      document.getElementById("show-all-marine-info-btn").onclick = () => showMarineInfoModal(data);
    }, 50);
  }
  document.getElementById("marineInfoJson").value = JSON.stringify(data); // 폼 전송 전에 저장

};
