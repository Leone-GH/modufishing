import { getSelectedFishTypes, setSelectedFishTypes } from "./modal_state.js";

// ✅ 모달 닫기 함수
function closeModal(modal) {
  modal?.classList.remove("show");
  modal?.classList.add("hidden");
}

/**
 * ✅ 어종 모달 초기화
 * @param {Object} options - 설정 객체
 * @param {Function} options.onApply - 어종 적용 시 실행할 외부 콜백 함수
 */
export function initFishModal({ onApply } = {}) {
  const fishBtn = document.getElementById("fishBtn");
  const fishModal = document.getElementById("fishModal");
  const fishList = document.getElementById("fishList");
  const fishApply = document.getElementById("fishApply");
  const fishReset = document.getElementById("fishReset");

  if (!fishBtn || !fishModal || !fishList || !fishApply || !fishReset) {
    console.warn("⚠️ [initFishModal] 필수 요소가 없음. HTML 확인 필요.");
    return;
  }

  // 🔘 모달 열기
  fishBtn.addEventListener("click", () => {
    fishModal.classList.remove("hidden");
    fishModal.classList.add("show");

    fishList.innerHTML = '';

    fetch("/api/fish-types")
      .then(res => res.json())
      .then(data => {
        data.sort((a, b) => a.localeCompare(b, 'ko'));
        const grouped = groupByInitial(data);
        fishList.innerHTML = renderGroupedFish(grouped);
        attachFishButtonEvents(fishModal);
      });
  });

  // 🔘 어종 적용
  fishApply.addEventListener("click", () => {
    closeModal(fishModal);
    if (typeof onApply === "function") onApply();
  });

  // 🔘 초기화
  fishReset.addEventListener("click", () => {
    setSelectedFishTypes([]);
    document.querySelectorAll(".fish-type-btn.selected").forEach(btn => btn.classList.remove("selected"));
    updateSelectedFishTextOnly(fishModal);
    if (typeof onApply === "function") onApply();
  });

  // ✅ 외부 클릭 시 닫기
  fishModal.addEventListener("click", (e) => {
    if (e.target.classList.contains("modal")) {
      closeModal(fishModal);
    }
  });
}

function groupByInitial(data) {
  const grouped = {};
  data.forEach(name => {
    const initial = getInitialConsonant(name);
    if (!grouped[initial]) grouped[initial] = [];
    grouped[initial].push(name);
  });
  return grouped;
}

function renderGroupedFish(grouped) {
  return Object.entries(grouped).map(([initial, names]) => {
    const groupHTML = names.map(name => {
      // 어종이 선택되어 있으면 'selected' 클래스 추가
      const isSelected = getSelectedFishTypes().includes(name);
      return `
        <button class="fish-type-btn ${isSelected ? 'selected' : ''}" data-fish="${name}">${name}</button>
      `;
    }).join("");

    return `
      <div class="fish-group">
        <div class="fish-group-title">[ ${initial} ]</div>
        <div class="fish-group-body">${groupHTML}</div>
      </div>`;
  }).join("");
}


function attachFishButtonEvents(modalRoot) {
  document.querySelectorAll(".fish-type-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      const value = btn.dataset.fish;
      btn.classList.toggle("selected");

      const types = getSelectedFishTypes();
      const idx = types.indexOf(value);
      if (idx !== -1) types.splice(idx, 1);
      else types.push(value);
      setSelectedFishTypes(types);

      updateSelectedFishTextOnly(modalRoot);
    });
  });
}


function updateSelectedFishTextOnly(modalRoot) {
  const types = getSelectedFishTypes();
  const text = types.length > 0 ? types.join(', ') : "선택된 어종 없음";
  const label = modalRoot.querySelector(".current-selection");
  if (label) label.textContent = text;
}


function getInitialConsonant(kor) {
  const initialTable = ["ㄱ","ㄲ","ㄴ","ㄷ","ㄸ","ㄹ","ㅁ","ㅂ","ㅃ","ㅅ","ㅆ","ㅇ","ㅈ","ㅉ","ㅊ","ㅋ","ㅌ","ㅍ","ㅎ"];
  const uni = kor.charCodeAt(0) - 44032;
  if (uni < 0 || uni > 11171) return "#";
  const index = Math.floor(uni / 588);
  return initialTable[index];
}

/**
 * ✅ 조건부 초기화
 */
export function initFishModalIfExist() {
  const fishBtn = document.getElementById("fishBtn");
  if (fishBtn) initFishModal();
}
