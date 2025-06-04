import {
  ModalState,
  injectHiddenInputs,
  openModal,
  closeModal,
  bindModalOutsideClick,
  getRequiredElements
} from "./modal_common.js";

// ✅ 지역 캐싱
let cachedRegions = null;
export function getCachedRegions() {
  return cachedRegions;
}
export function setCachedRegions(data) {
  cachedRegions = data;
}

/**
 * ✅ 지역 모달 초기화
 */
export function initRegionModal({ onApply } = {}) {
  const ids = {
    btn: "regionBtn",
    modal: "regionModal",
    list: "regionList",
    apply: "regionApply",
    reset: "regionReset",
    container: "regionIdsInput"
  };

  const el = getRequiredElements(ids);
  if (!el) return;

  // 🔘 모달 열기
    el.btn.addEventListener("click", () => {
      openModal(el.modal);

      const cached = getCachedRegions();
      if (cached) {
        renderFilteredRegions(cached, el.list, el.modal);
      } else {
        fetch("/api/regions/hierarchy")
          .then(res => {
            if (!res.ok) throw new Error("지역 데이터 응답 실패");
            return res.json();
          })
          .then(data => {
            setCachedRegions(data);
            renderFilteredRegions(data, el.list, el.modal);
          })
          .catch(err => {
            console.error("지역 데이터 로딩 실패:", err);
            el.list.innerHTML = '<p style="color:red;">지역 데이터를 불러오는 데 실패했습니다.</p>';
          });
      }
    });

  // 🔘 적용
  el.apply.addEventListener("click", () => {
    const selected = ModalState.getRegions().map(r => r.id);
    injectHiddenInputs(ids.container, "regionIds", selected);
    closeModal(el.modal);
    onApply?.();
  });

  // 🔘 초기화
  el.reset.addEventListener("click", () => {
    ModalState.setRegions([]);
    el.modal.querySelectorAll(".region-child-btn.selected").forEach(btn => btn.classList.remove("selected"));
    const label = el.modal.querySelector(".current-selection");
    if (label) label.textContent = "선택된 지역 없음";
    onApply?.();
  });

  // 🔘 외부 클릭 닫기
  bindModalOutsideClick(el.modal);
}

/**
 * ✅ 조건부 초기화
 */
export function initRegionModalIfExist({ onApply } = {}) {
  const requiredIds = ["regionBtn", "regionModal", "regionList", "regionApply", "regionReset"];
  const allExist = requiredIds.every(id => document.getElementById(id));
  if (allExist) {
    initRegionModal({ onApply });
  } else {
    console.warn("⚠️ [initRegionModalIfExist] 필수 요소 누락으로 초기화 생략");
  }
}

/**
 * ✅ 지역 필터 버튼 렌더링
 */
function renderFilteredRegions(data, container, modalRoot) {
  container.innerHTML = "";
  const selected = ModalState.getRegions();

  data.forEach(region => {
    const groupEl = document.createElement("div");
    groupEl.className = "region-group";

    const titleEl = document.createElement("div");
    titleEl.className = "region-title";
    titleEl.textContent = region.name;

    const childrenEl = document.createElement("div");
    childrenEl.className = "region-children";

    // 🔘 전체 선택 버튼
    const selectAllBtn = document.createElement("button");
    selectAllBtn.className = "region-child-btn region-select-all-btn";
    selectAllBtn.textContent = "전체";

    const allSelected = region.children.every(child =>
      selected.some(sel => sel.id === child.id)
    );
    if (allSelected) selectAllBtn.classList.add("selected");

    selectAllBtn.addEventListener("click", () => {
      const current = ModalState.getRegions();
      const next = [...current];
      const isAllSelected = region.children.every(child =>
        current.find(r => r.id === child.id)
      );

      region.children.forEach(child => {
        const btn = childrenEl.querySelector(`[data-region-id="${child.id}"]`);
        if (!btn) return;

        if (isAllSelected) {
          btn.classList.remove("selected");
          const idx = next.findIndex(r => r.id === child.id);
          if (idx !== -1) next.splice(idx, 1);
        } else {
          btn.classList.add("selected");
          if (!next.find(r => r.id === child.id)) {
            next.push({ id: child.id, name: child.name, parent: region.name });
          }
        }
      });

      selectAllBtn.classList.toggle("selected", !isAllSelected);
      ModalState.setRegions(next);
      updateSelectedRegionLabel(modalRoot);
    });

    childrenEl.appendChild(selectAllBtn);

    // 🔘 개별 버튼
    region.children.forEach(child => {
      const btn = document.createElement("button");
      btn.className = "region-child-btn";
      btn.textContent = child.name;
      btn.dataset.regionId = child.id;

      if (selected.some(r => r.id === child.id)) {
        btn.classList.add("selected");
      }

      btn.addEventListener("click", () => {
        const current = ModalState.getRegions();
        const next = [...current];

        const idx = next.findIndex(r => r.id === child.id);
        if (idx !== -1) {
          next.splice(idx, 1);
          btn.classList.remove("selected");
        } else {
          next.push({ id: child.id, name: child.name, parent: region.name });
          btn.classList.add("selected");
        }

        ModalState.setRegions(next);

        const isAllSelected = region.children.every(c =>
          next.find(r => r.id === c.id)
        );
        selectAllBtn.classList.toggle("selected", isAllSelected);
        updateSelectedRegionLabel(modalRoot);
      });

      childrenEl.appendChild(btn);
    });

    groupEl.appendChild(titleEl);
    groupEl.appendChild(childrenEl);
    container.appendChild(groupEl);
  });
}

/**
 * ✅ 라벨 텍스트 갱신
 */
function updateSelectedRegionLabel(modalRoot) {
  const selected = ModalState.getRegions();
  const grouped = {};

  selected.forEach(r => {
    const parent = r.parent || "기타";
    if (!grouped[parent]) grouped[parent] = [];
    grouped[parent].push(r.name);
  });

  const labelText = Object.entries(grouped).map(([parent, names]) => {
    const region = cachedRegions?.find(r => r.name === parent);
    const isAll = region?.children.every(child => names.includes(child.name));
    return isAll ? `${parent}(전체)` : names.map(name => `(${parent}) ${name}`).join(", ");
  }).join(", ");

  const label = modalRoot.querySelector(".current-selection");
  if (label) label.textContent = labelText || "선택된 지역 없음";
}
