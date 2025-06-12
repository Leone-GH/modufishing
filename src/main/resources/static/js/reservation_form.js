// ✅ 공통 모듈 import
import {
  ModalState,
  injectHiddenInputs,
  bindModalOutsideClick
} from "./modal_common.js";

import { getCachedRegions, setCachedRegions } from "./modal_region.js";
import { initRegionModalIfExist } from "./modal_region.js";
import { initFishModalIfExist } from "./modal_fish.js";
import { initDateModalIfExist } from "./modal_date.js";

// ✅ [1] 지역 데이터 초기화
fetch("/api/regions/hierarchy")
  .then(res => res.json())
  .then(setCachedRegions)
  .catch(err => console.error("지역 데이터 초기화 실패:", err));

// ✅ [2] 초기 진입 시 동작
window.addEventListener("DOMContentLoaded", () => {
  initAllModals();
  bindCurrencyInputField();
  bindMergedTimeBeforeSubmit(); // 폼 제출 시 날짜+시간+정원 -> hidden 필드로 병합

  // ✅ [🎯 수정폼 전용: 기존 예약 날짜 데이터 초기 세팅]
  const existingJsonEl = document.getElementById("existingDatesJson");
  if (existingJsonEl) {
    const rawDates = JSON.parse(existingJsonEl.value);
    const parsedDates = rawDates.map(d => {
      const [start, end] = d.time.split("~").map(s => s.trim());
      return {
        date: d.availableDate,
        start: start,
        end: end,
        capacity: d.capacity
      };
    });
    ModalState.setDates(parsedDates);   // 상태에 넣고
    updateDateLabel();                  // UI 그려줌
  }
});

// ✅ [3] 모달 초기화
function initAllModals() {
  initRegionModalIfExist({ onApply: updateRegionLabel });
  initFishModalIfExist({ onApply: updateFishLabel });
  initDateModalIfExist({ onApply: updateDateLabel });

  ["regionModal", "dateModal", "fishModal"].forEach(id => {
    const modal = document.getElementById(id);
    if (modal) bindModalOutsideClick(modal);
  });
}

// ✅ [4] 지역 UI 갱신
function updateRegionLabel() {
  const selected = ModalState.getRegions();
  const regionHierarchy = getCachedRegions();
  const grouped = {};

  selected.forEach(r => {
    const parent = r.parent || "기타";
    if (!grouped[parent]) grouped[parent] = [];
    grouped[parent].push(r.name);
  });

  const text = Object.entries(grouped).map(([parent, names]) => {
    const region = regionHierarchy?.find(r => r.name === parent);
    const isAll = region?.children.every(child => names.includes(child.name));
    return isAll ? `${parent}(전체)` : names.map(name => `(${parent}) ${name}`).join(", ");
  }).join(", ");

  const regionTextEl = document.getElementById("selectedRegionText");
  if (regionTextEl) {
    regionTextEl.textContent = text ? `선택 지역: ${text}` : "선택 지역: 없음";
    regionTextEl.className = "selection-result";
  }

  const modalLabel = document.querySelector("#regionModal .current-selection");
  if (modalLabel) modalLabel.textContent = text || "선택된 지역 없음";

  const ids = selected.map(r => r.id);
  injectHiddenInputs("regionIdsInput", "regionIds", ids);
}

// ✅ [5] 어종 UI 갱신
function updateFishLabel() {
  const selected = ModalState.getFishTypes();
  const text = selected.length > 0 ? selected.join(", ") : "";

  const fishTextEl = document.getElementById("selectedFishText");
  if (fishTextEl) {
    fishTextEl.textContent = text ? `선택 어종: ${text}` : "선택 어종: 없음";
    fishTextEl.className = "selection-result";
  }

  const modalLabel = document.querySelector("#fishModal .current-selection");
  if (modalLabel) modalLabel.textContent = text || "선택된 어종 없음";

  injectHiddenInputs("fishTypeInputGroup", "fishTypeNames", selected);
}

// ✅ [6] 날짜 + 시간 + 정원 UI 갱신
function updateDateLabel() {
  const selected = ModalState.getDates();
  const container = document.querySelector('#dateContainer[data-form-mode="true"]');
  if (!container) return;

  container.innerHTML = "";

  selected.forEach((entry, idx) => {
    const wrapper = document.createElement("div");
    wrapper.className = "date-entry";

    wrapper.innerHTML = `
      <span class="date-label">${entry.date}</span>
      <input type="text" class="timepicker start" name="startTimes[${idx}]" placeholder="시작 시간" value="${entry.start || ''}" required />
      <input type="text" class="timepicker end" name="endTimes[${idx}]" placeholder="종료 시간" value="${entry.end || ''}" required />
      <input type="number" class="capacity" name="capacities[${idx}]" placeholder="정원" min="1" value="${entry.capacity || ''}" required />
      <button type="button" class="remove-date" data-date="${entry.date}">&times;</button>
    `;

    container.appendChild(wrapper);
  });

  // ✅ 시간 입력 필드에 flatpickr 바인딩
  container.querySelectorAll(".timepicker").forEach(el => {
    flatpickr(el, {
      enableTime: true,
      noCalendar: true,
      dateFormat: "H:i",
      time_24hr: true,
      locale: 'ko'
    });
  });

  // ✅ 삭제 버튼 이벤트 바인딩
  container.querySelectorAll(".remove-date").forEach(btn => {
    btn.addEventListener("click", () => {
      const dateToRemove = btn.getAttribute("data-date");
      ModalState.removeDate(dateToRemove);
      updateDateLabel();
    });
  });
}

// ✅ [7] 가격 필드 ₩ 포맷 처리
function formatCurrencyInput(value) {
  const number = Number(value.replace(/[^\d]/g, ''));
  if (isNaN(number)) return '';
  return '₩' + number.toLocaleString("ko-KR");
}

function bindCurrencyInputField() {
  const input = document.getElementById("price");
  const hidden = document.getElementById("priceRaw");

  if (!input || !hidden) return;

  input.addEventListener("input", () => {
    const raw = input.value.replace(/[^\d]/g, '');
    const num = Number(raw);
    input.value = formatCurrencyInput(raw);
    hidden.value = isNaN(num) ? '' : num;
  });

  const initRaw = input.value.replace(/[^\d]/g, '');
  input.value = formatCurrencyInput(initRaw);
  hidden.value = initRaw;
}

// ✅ [8] 날짜/시간/정원 -> 서버 전송용 hidden input 생성
function bindMergedTimeBeforeSubmit() {
  const form = document.getElementById("reservationForm") || document.getElementById("reservationEditForm"); // ✅ 두 폼 다 지원

  if (!form) return;

  form.addEventListener("submit", () => {
    const dateEntries = document.querySelectorAll(".date-entry");

    dateEntries.forEach((entry, idx) => {
      const date = entry.querySelector(".date-label")?.textContent || "";
      const start = entry.querySelector(".timepicker.start")?.value || "";
      const end = entry.querySelector(".timepicker.end")?.value || "";
      const capacity = entry.querySelector(".capacity")?.value || "";

      const dateInput = document.createElement("input");
      dateInput.type = "hidden";
      dateInput.name = `availableDates[${idx}].date`;
      dateInput.value = date;
      form.appendChild(dateInput);

      const timeInput = document.createElement("input");
      timeInput.type = "hidden";
      timeInput.name = `availableDates[${idx}].time`;
      timeInput.value = `${start}~${end}`;
      form.appendChild(timeInput);

      const capacityInput = document.createElement("input");
      capacityInput.type = "hidden";
      capacityInput.name = `availableDates[${idx}].capacity`;
      capacityInput.value = capacity;
      form.appendChild(capacityInput);
    });
  });
}

window.ModalState = ModalState;