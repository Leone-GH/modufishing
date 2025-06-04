import { getSelectedRegions, setSelectedRegions } from "./modal_state.js";

// ✅ 지역 계층 데이터 캐싱 변수
let cachedRegions = null;

// ✅ 외부에서 접근할 수 있도록 export
export function getCachedRegions() {
  return cachedRegions;
}

export function setCachedRegions(data) {
  cachedRegions = data;
}

// ✅ 모달 닫기 함수
function closeModal(modal) {
  modal?.classList.remove("show");
  modal?.classList.add("hidden");
}

// ✅ 지역 모달 초기화 함수 (콜백 포함)
export function initRegionModal({ onApply } = {}) {
  const regionBtn = document.getElementById("regionBtn");
  const regionModal = document.getElementById("regionModal");
  const regionList = document.getElementById("regionList");
  const regionApply = document.getElementById("regionApply");
  const regionReset = document.getElementById("regionReset");

  if (!regionBtn || !regionModal || !regionList || !regionApply || !regionReset) {
    console.warn("⚠️ [initRegionModal] 필수 요소가 없음. HTML 확인 필요.");
    return;
  }

  regionBtn.addEventListener("click", () => {
    regionModal.classList.remove("hidden");
    regionModal.classList.add("show");

    const cached = getCachedRegions();
    if (cached) {
      renderFilteredRegions(cached, regionList, regionModal);
    } else {
      fetch("/api/regions/hierarchy")
        .then(res => res.json())
        .then(data => {
          setCachedRegions(data);
          renderFilteredRegions(data, regionList, regionModal);
        })
        .catch(err => {
          console.error("지역 데이터 로딩 실패:", err);
          regionList.innerHTML = '<p style="color:red;">지역 데이터를 불러오는 데 실패했습니다.</p>';
        });
    }
  });

  regionApply.addEventListener("click", () => {
    closeModal(regionModal);
    if (typeof onApply === "function") onApply();
  });

  regionReset.addEventListener("click", () => {
    setSelectedRegions([]);
    document.querySelectorAll(".region-child-btn.selected").forEach(btn => btn.classList.remove("selected"));
    const label = regionModal.querySelector(".current-selection");
    if (label) label.textContent = "선택된 지역 없음";
    if (typeof onApply === "function") onApply();
  });

  regionModal.addEventListener("click", (e) => {
    if (e.target.classList.contains("modal")) {
      closeModal(regionModal);
    }
  });
}

// ✅ 조건부 초기화
export function initRegionModalIfExist(onApply) {
  const regionBtn = document.getElementById("regionBtn");
  if (regionBtn) initRegionModal({ onApply });
}

// ✅ 지역 리스트 렌더링
// 지역 리스트 렌더링 수정 (선택된 지역 반영)
function renderFilteredRegions(data, container, modalRoot) {
  container.innerHTML = '';

  const selectedRegions = getSelectedRegions();

  data.forEach(region => {
    const parentWrapper = document.createElement('div');
    parentWrapper.className = 'region-group';

    const title = document.createElement('div');
    title.className = 'region-title';
    title.innerText = region.name;
    parentWrapper.appendChild(title);

    const childWrapper = document.createElement('div');
    childWrapper.className = 'region-children';

    // 🔘 전체 버튼 (같은 줄에 배치되도록 childWrapper에 포함)
    const selectAllBtn = document.createElement('button');
    selectAllBtn.className = 'region-child-btn region-select-all-btn';
    selectAllBtn.innerText = '전체';

    // 전체 버튼 상태 반영
    const allSelected = region.children.every(child =>
      selectedRegions.some(r => r.id == child.id)
    );
    if (allSelected) {
      selectAllBtn.classList.add("selected");
    }

    selectAllBtn.addEventListener("click", () => {
      const allSelected = region.children.every(child =>
        getSelectedRegions().some(r => r.id == child.id)
      );

      // 전체 선택 상태 토글
      if (allSelected) {
        selectAllBtn.classList.remove("selected");
      } else {
        selectAllBtn.classList.add("selected");
      }

      region.children.forEach(child => {
        const btn = childWrapper.querySelector(`.region-child-btn[data-region-id="${child.id}"]`);
        if (!btn) return;

        if (allSelected) {
          btn.classList.remove("selected");
          removeRegion(child);
        } else {
          btn.classList.add("selected");
          addRegion({ id: child.id, name: child.name, parent: region.name });
        }
      });

      updateSelectedRegionLabel(modalRoot);
    });

    // 전체 버튼 먼저 추가
    childWrapper.appendChild(selectAllBtn);

    // 🔘 자식 버튼들
    region.children.forEach(child => {
      const btn = document.createElement('button');
      btn.className = 'region-child-btn';
      btn.innerText = child.name;
      btn.dataset.regionId = child.id;

      // 이미 선택된 지역은 "selected" 클래스 추가
      if (selectedRegions.some(r => r.id === child.id)) {
        btn.classList.add('selected');
      }

      btn.addEventListener("click", () => {
        btn.classList.toggle("selected");

        const idx = getSelectedRegions().findIndex(r => r.id == child.id);
        if (idx !== -1) {
          removeRegion(child);
        } else {
          addRegion({ id: child.id, name: child.name, parent: region.name });
        }

        // 전체 버튼도 상태 업데이트
        const allNowSelected = region.children.every(child =>
          getSelectedRegions().some(r => r.id == child.id)
        );
        if (allNowSelected) {
          selectAllBtn.classList.add("selected");
        } else {
          selectAllBtn.classList.remove("selected");
        }

        updateSelectedRegionLabel(modalRoot);
      });

      childWrapper.appendChild(btn);
    });

    parentWrapper.appendChild(childWrapper);
    container.appendChild(parentWrapper);
  });
}

// ✅ 선택된 지역 추가
function addRegion(region) {
  const selected = getSelectedRegions();
  if (!selected.find(r => r.id === region.id)) {
    selected.push({
      id: region.id,
      name: region.name,
      parent: region.parent || ""
    });
    setSelectedRegions(selected);
  }
}

// ✅ 선택된 지역 제거
function removeRegion(region) {
  const selected = getSelectedRegions();
  const idx = selected.findIndex(r => r.id === region.id);
  if (idx !== -1) {
    selected.splice(idx, 1);
    setSelectedRegions(selected);
  }
}

// ✅ 선택 라벨 업데이트
function updateSelectedRegionLabel(modalRoot) {
  const selected = getSelectedRegions();
  const grouped = {};

  selected.forEach(r => {
    const parent = r.parent || "기타";
    if (!grouped[parent]) grouped[parent] = [];
    grouped[parent].push(r.name);
  });

  const labelText = Object.entries(grouped).map(([parent, names]) => {
    const isAll = cachedRegions?.find(r => r.name === parent)?.children.every(child =>
      selected.find(s => s.name === child.name)
    );
    return isAll ? `${parent}(전체)` : names.map(name => `(${parent}) ${name}`).join(", ");
  }).join(", ");

  const label = modalRoot.querySelector(".current-selection");
  if (label) label.textContent = labelText || "선택된 지역 없음";
}
