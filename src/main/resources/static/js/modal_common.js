// modal_common.js

// ----------------------------------------
// [1] 상태 관리 모듈 (ModalState)
// ----------------------------------------

let selectedFishTypes = [];
let selectedRegions = [];
let selectedDates = [];  // ✅ [{ date, start, end, capacity }] 구조로 변경

export const ModalState = {
  getFishTypes: () => [...selectedFishTypes],
  setFishTypes: (types) => {
    selectedFishTypes = Array.isArray(types) ? [...types] : [];
  },

  getRegions: () => [...selectedRegions],
  setRegions: (regions) => {
    selectedRegions = Array.isArray(regions) ? [...regions] : [];
  },

  getDates: () => [...selectedDates],
  setDates: (dates) => {
    selectedDates = Array.isArray(dates) ? [...dates] : [];
  },

  /**
   * ✅ 특정 날짜를 기준으로 해당 항목 제거
   * @param {string} date
   */
  removeDate: (date) => {
    selectedDates = selectedDates.filter(d => d.date !== date);
  },

  reset: () => {
    selectedFishTypes = [];
    selectedRegions = [];
    selectedDates = [];
  }
};



// ----------------------------------------
// [2] Hidden Input 삽입 유틸
// ----------------------------------------

/**
 * ✅ 지정된 컨테이너에 name=value 형식의 hidden input을 삽입
 * @param {string} containerId
 * @param {string} name
 * @param {Array<string|number>} values
 */
export function injectHiddenInputs(containerId, name, values) {
  const container = document.getElementById(containerId);
  if (!container) {
    console.warn(`[injectHiddenInputs] 컨테이너 ID '${containerId}' 없음`);
    return;
  }

  container.innerHTML = "";

  if (!Array.isArray(values)) return;

  values.forEach((value, index) => {
    if (value !== null && value !== undefined && value !== "") {
      const input = document.createElement("input");
      input.type = "hidden";
      input.name = `${name}[${index}]`; // 배열 인덱스 기반 이름 부여
      input.value = value;
      container.appendChild(input);
    }
  });
}

// ----------------------------------------
// [3] 공통 모달 유틸
// ----------------------------------------

/**
 * ✅ 모달 열기
 */
export function openModal(modal) {
  if (!modal) return;
  modal.classList.remove("hidden");
  modal.classList.add("show");
}

/**
 * ✅ 모달 닫기
 */
export function closeModal(modal) {
  if (!modal) return;
  modal.classList.remove("show");
  modal.classList.add("hidden");
}

/**
 * ✅ 외부 클릭 시 모달 닫기
 * @param {HTMLElement} modal
 * @param {Function} [onClose]
 */
export function bindModalOutsideClick(modal, onClose) {
  if (!modal) return;
  modal.addEventListener("click", (e) => {
    if (e.target.classList.contains("modal")) {
      closeModal(modal);
      onClose?.();
    }
  });
}

/**
 * ✅ ID 맵 기준으로 요소 가져오기 (모두 존재하면 반환, 아니면 null)
 * @param {Object} idMap
 * @returns {Object|null}
 */
export function getRequiredElements(idMap) {
  const elements = {};
  for (const key in idMap) {
    const el = document.getElementById(idMap[key]);
    if (!el) {
      console.warn(`⚠️ [modal_common] 필수 요소 누락: ${idMap[key]}`);
      return null;
    }
    elements[key] = el;
  }
  return elements;
}

window.ModalState = ModalState;
