import { getSelectedRegions, getSelectedFishTypes, closeModal } from "./modal_state.js";
import { initRegionModalIfExist } from "./modal_region.js";
import { initFishModalIfExist } from "./modal_fish.js";
import { initDateModalIfExist } from "./modal_date.js";
import { getCachedRegions } from "./modal_region.js";

// ✅ flatpickr 전역 객체 사용 (CDN으로 이미 로드되어 있어야 함)
flatpickr.localize(flatpickr.l10ns.ko); // 한글 로케일 설정

// ✅ 지역명 출력용 유틸
function getCompactRegionText(selectedRegions, regionHierarchy) {
  const grouped = {};
  selectedRegions.forEach(r => {
    const parent = r.parent || "기타";
    if (!grouped[parent]) grouped[parent] = [];
    grouped[parent].push(r.name);
  });

  return Object.entries(grouped).map(([parent, names]) => {
    const region = regionHierarchy.find(r => r.name === parent);
    const isAll = region && region.children.every(child =>
      selectedRegions.find(sel => sel.name === child.name)
    );
    return isAll ? `${parent}(전체)` : names.map(name => `(${parent}) ${name}`).join(", ");
  }).join(", ");
}

// ✅ DOM 요소
const regionApplyBtn = document.getElementById("regionApply");
const fishApplyBtn = document.getElementById("fishApply");
const regionIdInputContainer = document.getElementById("regionIdInput"); // div로 바꾼 부분
const fishTypeInputGroup = document.getElementById("fishTypeInputGroup");
const selectedRegionOutput = document.getElementById("selectedRegionText");
const selectedFishOutput = document.getElementById("selectedFishText");

// ✅ 지역 적용 버튼 클릭 시
regionApplyBtn?.addEventListener("click", () => {
  const regions = getSelectedRegions();
  const cached = getCachedRegions();
  const label = getCompactRegionText(regions, cached);
  selectedRegionOutput.textContent = label || "선택된 지역 없음";

  // ✅ input 비우고 다시 생성
  regionIdInputContainer.innerHTML = "";
  regions.forEach(region => {
    const input = document.createElement("input");
    input.type = "hidden";
    input.name = "regionIds"; // DTO에 맞게 name 설정
    input.value = region.id;
    regionIdInputContainer.appendChild(input);
  });
});

// ✅ 어종 적용 버튼 클릭 시
fishApplyBtn?.addEventListener("click", () => {
  const fish = getSelectedFishTypes();
  selectedFishOutput.textContent = fish.length > 0 ? fish.join(", ") : "선택된 어종 없음";

  fishTypeInputGroup.innerHTML = '';
  fish.forEach(name => {
    const input = document.createElement("input");
    input.type = "hidden";
    input.name = "fishTypeNames";
    input.value = name;
    fishTypeInputGroup.appendChild(input);
  });
});

// ✅ 모달 외부 클릭 시 닫기
function initModalOutsideClose() {
  [document.getElementById("regionModal"), document.getElementById("dateModal"), document.getElementById("fishModal")]
    .forEach(modal => {
      modal?.addEventListener("click", (e) => {
        if (e.target.classList.contains("modal")) {
          closeModal(modal);
        }
      });
    });
}

// ✅ 최초 초기화
document.addEventListener("DOMContentLoaded", () => {
  initRegionModalIfExist();
  initFishModalIfExist();
  initDateModalIfExist();
  initModalOutsideClose();
});

// ✅ flatpickr 달력 설정
flatpickr("#datePicker", {
  locale: "ko",
  mode: "multiple",
  dateFormat: "Y-m-d",
  position: "auto left top",
  positionElement: document.getElementById("datePicker"),

  onDayCreate: function (_, __, ___, dayElem) {
    const day = dayElem.dateObj.getDay();
    if (day === 0) dayElem.classList.add("sunday");
    else if (day === 6) dayElem.classList.add("saturday");
  },

  // ✅ 날짜 선택 시 정원/시간 입력 필드 생성
  onChange: (selectedDates, dateStr, instance) => {
    const container = document.getElementById("dateContainer");
    container.innerHTML = "";

    selectedDates.forEach((date, idx) => {
      const koreaOffset = 9 * 60 * 60000;
      const koreaDate = new Date(date.getTime() + koreaOffset);
      const formatted = koreaDate.toISOString().split("T")[0];

      const div = document.createElement("div");
      div.className = "date-entry";
      div.innerHTML = `
        <label>${formatted}</label>
        <input type="hidden" name="availableDates[${idx}].date" value="${formatted}">
        <input type="text" name="availableDates[${idx}].time" placeholder="예: 06:00~14:00" pattern="^\\d{2}:\\d{2}~\\d{2}:\\d{2}$" required>
        <input type="number" name="availableDates[${idx}].capacity" placeholder="정원" min="1" required>
        <button type="button" class="remove-date" data-date="${formatted}">❌</button>
      `;
      container.appendChild(div);
    });

    container.querySelectorAll(".remove-date").forEach(btn => {
      btn.addEventListener("click", () => {
        const dateToRemove = btn.dataset.date;
        const updatedDates = selectedDates.filter(date => {
          const koreaDate = new Date(date.getTime() + 9 * 60 * 60000);
          return koreaDate.toISOString().split("T")[0] !== dateToRemove;
        });
        instance.setDate(updatedDates, true);
      });
    });
  }
});
