import {
  ModalState,
  closeModal,
  bindModalOutsideClick,
  injectHiddenInputs
} from "./modal_common.js";

import { initRegionModal } from "./modal_region.js";
import { initFishModal } from "./modal_fish.js";
import { initDateModal } from "./modal_date.js";

// ✅ 지역 캐시 전역 저장
let cachedRegions = null;

export function getCachedRegions() {
  return cachedRegions;
}

export function setCachedRegions(data) {
  cachedRegions = data;
  if (ModalState.setRegionCache) {
    ModalState.setRegionCache(data);
  }
}

// ✅ 필터링 함수 - 선택된 조건 기반 API 호출
export function applyFilters({ sortKey = "latest" } = {}) {
  const type = location.pathname.split("/").at(-1);  // URL에서 타입 추출
  const query = new URLSearchParams({ type, page: 0, sort: sortKey });

  ModalState.getRegions().forEach(r => query.append("regionId", r.id));
  ModalState.getDates().forEach(d => query.append("date", d.date));
  ModalState.getFishTypes().forEach(f => query.append("fishType", f));

  const keywordEl = document.querySelector(".search-input");
  const keyword = keywordEl ? keywordEl.value.trim() : "";
  if (keyword) query.append("keyword", keyword);

  console.log("FILTER QUERY =", query.toString());

  fetch(`/api/reservation?${query.toString()}`)
    .then(res => res.ok ? res.json() : Promise.reject("서버 오류"))
    .then(data => Array.isArray(data) ? updateCards(data) : Promise.reject("데이터 오류"))
    .catch(err => {
      console.error("카드 불러오기 실패:", err);
      const container = document.getElementById("cardContainer");
      if (container) {
        container.innerHTML = '<p style="text-align:center; color:red;">카드 데이터를 불러오지 못했습니다.</p>';
      }
    });
}

// ✅ 카드 리스트 렌더링
function updateCards(cards) {
  const container = document.getElementById("cardContainer");
  if (!container) return;

  const typeToExtension = {
    boat: "jpg",
    float: "png",
    island: "jpg",
    rock: "jpg",
    stay: "png"
  };

  container.innerHTML = cards.length === 0
    ? '<p style="text-align:center;">조건에 맞는 예약이 없습니다.</p>'
    : cards.map(card => {
        const ext = typeToExtension[card.typeLower] || "jpg";
        const imageSrc = card.imageUrl || `/images/${card.typeLower || 'boat'}.${ext}`;

        return `
          <div class="ad-card">
            <a href="/reservation/detail/${card.id}" class="ad-card-link">
              <div class="ad-image">
                <img src="${imageSrc}" alt="예약 이미지" class="card-image">
              </div>
              <div class="ad-desc">${card.title}</div>
              <div class="ad-detail">
                <p>${card.companyName || "알 수 없음"}</p>
                <p>${card.region || "지역 없음"}</p>
                <p>${(card.fishTypes && card.fishTypes.join(", ")) || "어종 없음"}</p>
              </div>
            </a>
          </div>
        `;
      }).join('');
}

// ✅ 선택된 지역 텍스트 포맷
function getCompactRegionText() {
  const regions = ModalState.getRegions();
  const cached = getCachedRegions();
  if (!cached) return "선택된 지역 없음";

  const grouped = regions.reduce((acc, cur) => {
    (acc[cur.parent] = acc[cur.parent] || []).push(cur);
    return acc;
  }, {});

  return Object.entries(grouped)
    .flatMap(([parent, selected]) => {
      const total = (cached.find(r => r.name === parent) || {}).children?.length || 0;
      return selected.length === total
        ? [`(${parent}) 전체`]
        : selected.map(c => `(${parent}) ${c.name}`);
    })
    .join(", ");
}

// ✅ 선택 정보 UI에 표시
function updateSelectedInfo() {
  const region = ModalState.getRegions();
  const fish = ModalState.getFishTypes();
  const date = ModalState.getDates();

  const label = document.getElementById("selectedInfo");
  if (!label) return;

  const parts = [];

  if (region.length > 0) {
    parts.push(`선택 지역: ${getCompactRegionText()}`);
  }

  if (date.length > 0) {
    const formattedDates = date.map(d => d.date).join(", ");
    parts.push(`선택 날짜: ${formattedDates}`);
  }

  if (fish.length > 0) {
    parts.push(`선택 어종: ${fish.join(", ")}`);
  }

  label.innerText = parts.join("\n");
}

// ✅ 정렬 이벤트
function initSortControl() {
  const sortBtn = document.getElementById("sortBtn");
  const sortOptions = document.getElementById("sortOptions");
  if (!sortBtn || !sortOptions) return;

  sortBtn.addEventListener("click", () => sortOptions.classList.toggle("hidden"));

  document.querySelectorAll("#sortOptions button").forEach(btn => {
    btn.addEventListener("click", () => {
      const selected = btn.getAttribute("data-sort");
      applyFilters({ sortKey: selected });
      sortOptions.classList.add("hidden");
    });
  });

  document.addEventListener("click", (e) => {
    if (!sortBtn.contains(e.target) && !sortOptions.contains(e.target)) {
      sortOptions.classList.add("hidden");
    }
  });
}

// ✅ 검색창 동작
function initSearchControl() {
  const input = document.querySelector(".search-input");
  const btn = document.querySelector(".search-button");
  if (btn) btn.addEventListener("click", () => applyFilters({}));
  if (input) input.addEventListener("keydown", (e) => {
    if (e.key === "Enter") applyFilters({});
  });
}

// ✅ 모달 외부 클릭 시 닫기
function initModalOutsideClose() {
  ["regionModal", "dateModal", "fishModal"].forEach(id => {
    const modal = document.getElementById(id);
    if (modal) bindModalOutsideClick(modal);
  });
}

// ✅ 모든 모달 초기화
function initAllModals() {
  initRegionModal({ onApply: handleRegionApply });
  initFishModal({ onApply: handleFishApply });
  initDateModalIfExist({ onApply: handleDateApply });
  initModalOutsideClose();
}

// ✅ 모달 필터 적용 시 실행되는 콜백들
function handleRegionApply() {
  updateSelectedInfo();
  if (isListPage) applyFilters();
}

function handleFishApply() {
  updateSelectedInfo();
  if (isListPage) applyFilters();
}

function handleDateApply() {
  updateSelectedInfo();
  if (isListPage) applyFilters();
}

// ✅ 예약 목록 페이지 여부 체크
const isListPage = location.pathname.includes("/reservation/");

// ✅ 지역 계층 정보 초기 fetch
fetch("/api/regions/hierarchy")
  .then(res => res.json())
  .then(setCachedRegions)
  .catch(err => console.error("지역 데이터 초기화 실패:", err));

// ✅ DOM 로드 완료 시 초기화 실행
document.addEventListener("DOMContentLoaded", () => {
  initSortControl();
  initSearchControl();
  initAllModals();
  applyFilters(); // 첫 로딩 시 카드 목록 불러오기
});

// ✅ 날짜 모달 조건부 초기화 함수
export function initDateModalIfExist({ onApply } = {}) {
  const requiredIds = [
    "dateBtn",
    "dateModal",
    "dateApply",
    "dateCancel",
    "dateReset",
    "dateContainer",
    "datePickerContainer"
  ];

  const allExist = requiredIds.every(id => document.getElementById(id));
  if (allExist) {
    initDateModal({ onApply });
  }
}
