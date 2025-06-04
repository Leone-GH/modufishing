import {
  ModalState,
  injectHiddenInputs,
  openModal,
  closeModal,
  bindModalOutsideClick,
  getRequiredElements
} from "./modal_common.js";

/**
 * ✅ 어종 모달 초기화
 */
export function initFishModal({ onApply } = {}) {
  const ids = {
    btn: "fishBtn",
    modal: "fishModal",
    list: "fishList",
    apply: "fishApply",
    reset: "fishReset",
    container: "fishTypeInputGroup",
  };

  const el = getRequiredElements(ids);
  if (!el) return;

  // 🔘 모달 열기
  el.btn.addEventListener("click", () => {
    openModal(el.modal);
    el.list.innerHTML = "";

    fetch("/api/fish-types")
      .then(res => {
        if (!res.ok) throw new Error("어종 데이터 응답 실패");
        return res.json();
      })
      .then(data => {
        data.sort((a, b) => a.localeCompare(b, "ko"));
        const grouped = groupByInitial(data);
        el.list.innerHTML = renderGroupedFish(grouped);
        attachFishButtonEvents(el.modal);
      })
      .catch(err => {
        console.error("어종 목록 불러오기 실패:", err);
        el.list.innerHTML = `<p style="color:red;">어종 데이터를 불러오는 데 실패했습니다.</p>`;
      });
  });

  // 🔘 적용
  el.apply.addEventListener("click", () => {
    injectHiddenInputs(ids.container, "fishTypeNames", ModalState.getFishTypes());
    closeModal(el.modal);
    onApply?.();
  });

  // 🔘 초기화
  el.reset.addEventListener("click", () => {
    ModalState.setFishTypes([]);
    el.modal.querySelectorAll(".fish-type-btn.selected").forEach(btn => btn.classList.remove("selected"));
    onApply?.();
  });

  // 🔘 외부 클릭으로 닫기
  bindModalOutsideClick(el.modal);
}

/**
 * ✅ 버튼 클릭 시 선택 상태 토글
 */
function attachFishButtonEvents(modalRoot) {
  modalRoot.querySelectorAll(".fish-type-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      const value = btn.dataset.fish;
      const current = ModalState.getFishTypes();
      let updated = [...current];

      if (current.includes(value)) {
        updated = updated.filter(v => v !== value);
        btn.classList.remove("selected");
      } else {
        updated.push(value);
        btn.classList.add("selected");
      }

      ModalState.setFishTypes(updated);

      const label = modalRoot.querySelector(".current-selection");
      if (label) {
        label.textContent = updated.length > 0 ? updated.join(", ") : "선택된 어종 없음";
      }
    });
  });
}

/**
 * ✅ 초성 기준으로 어종 그룹화
 */
function groupByInitial(data) {
  const grouped = {};
  data.forEach(name => {
    const initial = getInitialConsonant(name);
    if (!grouped[initial]) grouped[initial] = [];
    grouped[initial].push(name);
  });
  return grouped;
}

/**
 * ✅ 그룹 버튼 HTML 렌더링
 */
function renderGroupedFish(grouped) {
  return Object.entries(grouped).map(([initial, names]) => {
    const groupHTML = names.map(name => {
      const isSelected = ModalState.getFishTypes().includes(name);
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

/**
 * ✅ 한글 초성 추출
 */
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
export function initFishModalIfExist({ onApply } = {}) {
  const requiredIds = ["fishBtn", "fishModal", "fishList", "fishApply", "fishReset"];
  const allExist = requiredIds.every(id => document.getElementById(id));
  if (allExist) {
    initFishModal({ onApply });
  } else {
    console.warn("⚠️ [initFishModalIfExist] 필수 요소 누락으로 초기화 생략");
  }
}
