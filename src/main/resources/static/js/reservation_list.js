// reservation_list.js 맨 위에 추가
fetch("/api/regions/hierarchy")
  .then(res => res.json())
  .then(data => setCachedRegions(data))
  .catch(err => console.error("지역 데이터 초기화 실패:", err));

// 🔁 모듈 import
import { initRegionModal } from "./modal_region.js";
import { initDateModal } from "./modal_date.js";
import { initFishModal } from "./modal_fish.js";
import { getSelectedRegions, setSelectedRegions, getSelectedFishTypes, setSelectedFishTypes, selectedDate } from "./modal_state.js";

// ✅ 지역 캐시 + getter/setter
let cachedRegions = null;
export function getCachedRegions() {
  return cachedRegions;
}
export function setCachedRegions(data) {
  cachedRegions = data;
}

// ✅ 공통 모달 닫기 함수
export function closeModal(modal) {
  modal.classList.remove("show");
  modal.classList.add("hidden");
}

// ✅ 필터 실행 함수
export function applyFilters({ sortKey = "latest" }) {
  fetchFilteredCards(sortKey);
}

// ✅ 서버로 필터링된 카드 요청
export function fetchFilteredCards(sortKey = "latest") {
  const type = location.pathname.split("/").at(-1);
  const query = new URLSearchParams({ type, page: 0, sort: sortKey });

  getSelectedRegions().forEach(r => query.append("regionId", r.id));
  if (Array.isArray(selectedDate.value)) {
    selectedDate.value.forEach(date => query.append("date", date));
  } else if (selectedDate.value) {
    query.append("date", selectedDate.value);
  }

  getSelectedFishTypes().forEach(fish => query.append("fishType", fish));

  const keyword = document.querySelector(".search-input")?.value.trim();
  if (keyword) query.append("keyword", keyword);

  fetch(`/api/reservation?${query.toString()}`)
    .then(res => res.ok ? res.json() : Promise.reject("서버 오류"))
    .then(data => Array.isArray(data) ? updateCards(data) : Promise.reject("데이터 오류"))
    .catch(err => {
      console.error("카드 불러오기 실패:", err);
      const container = document.getElementById("cardContainer");
      container.innerHTML = '<p style="text-align:center; color:red;">카드 데이터를 불러오지 못했습니다.</p>';
    });
}

// ✅ 카드 DOM 업데이트
function updateCards(cards) {
  const container = document.getElementById("cardContainer");
  container.innerHTML = cards.length === 0
    ? '<p style="text-align:center;">조건에 맞는 예약이 없습니다.</p>'
    : cards.map(card => `
      <div class="ad-card">
        <a href="/reservation/detail/${card.id}" class="ad-card-link">
          <div class="ad-image">
            <img src="${card.imageUrl || "/images/boat.jpg"}" alt="예약 이미지" class="card-image">
          </div>
          <div class="ad-desc">${card.title}</div>
          <div class="ad-detail">
            <p>지역: ${card.region ?? "없음"}</p>
            <p>회사명: ${card.companyName ?? "알 수 없음"}</p>
            <p>어종: ${card.fishTypes?.join(", ") ?? "정보 없음"}</p>
            <p>${card.content}</p>
          </div>
        </a>
      </div>
    `).join('');
}

// 🔧 지역 텍스트 조합 유틸 함수
function getCompactRegionText() {
  const regions = getSelectedRegions();
  const cached = getCachedRegions();
  if (!cached) return "선택된 지역 없음";

  const grouped = regions.reduce((acc, cur) => {
    (acc[cur.parent] = acc[cur.parent] || []).push(cur);
    return acc;
  }, {});

  return Object.entries(grouped)
    .flatMap(([parentName, selectedChildren]) => {
      const parentRegion = cached.find(r => r.name === parentName);
      const totalChildren = parentRegion?.children?.length || 0;

      return selectedChildren.length === totalChildren
        ? [`(${parentName}) 전체`]
        : selectedChildren.map(c => `(${parentName}) ${c.name}`);
    })
    .join(", ");
}


// ✅ 선택된 지역 텍스트 갱신
export function updateSelectedRegionTextOnly() {
  const modalDiv = document.querySelector("#regionModal .current-selection");
  const pageDiv = document.getElementById("selectedInfo");
  const regions = getSelectedRegions();
  let text = "선택된 지역 없음";

  if (regions.length > 0) {
    text = `현재 선택 지역: ${getCompactRegionText()}`;
  }

  modalDiv.innerText = text;
  const dateText = selectedDate.value ? `선택한 날짜: ${selectedDate.value}` : "";
  pageDiv.innerText = [text, dateText].filter(Boolean).join("\n");
}

// ✅ 선택된 날짜 텍스트 갱신
export function updateSelectedDateTextOnly() {
  const modalDiv = document.querySelector("#dateModal .current-selection");
  const pageDiv = document.getElementById("selectedInfo");
  let dateText = "선택된 날짜 없음";
  if (Array.isArray(selectedDate.value)) {
    dateText = `선택한 날짜: ${selectedDate.value.join(", ")}`;
  } else if (selectedDate.value) {
    dateText = `선택한 날짜: ${selectedDate.value}`;
  }

  modalDiv.innerText = dateText;

  const regionText = getSelectedRegions().length > 0
    ? `현재 선택 지역: ${getCompactRegionText()}`
    : "";
  pageDiv.innerText = [regionText, dateText].filter(Boolean).join("\n");
}

// ✅ 선택된 어종 텍스트 갱신
export function updateSelectedFishText() {
  const modalDiv = document.querySelector("#fishModal .current-selection");
  const pageDiv = document.getElementById("selectedInfo");
  const fish = getSelectedFishTypes();
  const fishText = fish.length > 0 ? `선택한 어종: ${fish.join(", ")}` : "선택된 어종 없음";
  modalDiv.innerText = fishText;

  const regionText = getSelectedRegions().length > 0
    ? `현재 선택 지역: ${getCompactRegionText()}`
    : "";
  const dateText = selectedDate.value ? `선택한 날짜: ${selectedDate.value}` : "";
  pageDiv.innerText = [regionText, dateText, fishText].filter(Boolean).join("\n");
}

// ✅ 정렬 컨트롤 초기화
function initSortControl() {
  const sortBtn = document.getElementById("sortBtn");
  const sortOptions = document.getElementById("sortOptions");

  sortBtn?.addEventListener("click", () => {
    sortOptions.classList.toggle("hidden");
  });

  document.querySelectorAll("#sortOptions button").forEach(btn => {
    btn.addEventListener("click", () => {
      const selectedSort = btn.getAttribute("data-sort");
      applyFilters({ sortKey: selectedSort });
      sortOptions.classList.add("hidden");
    });
  });

  document.addEventListener("click", (e) => {
    if (!sortBtn.contains(e.target) && !sortOptions.contains(e.target)) {
      sortOptions.classList.add("hidden");
    }
  });
}

// ✅ 검색 컨트롤 초기화
function initSearchControl() {
  const searchInput = document.querySelector(".search-input");
  const searchButton = document.querySelector(".search-button");

  searchButton?.addEventListener("click", () => {
    applyFilters({});
  });

  searchInput?.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      applyFilters({});
    }
  });
}

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

// ✅ 초기화
document.addEventListener("DOMContentLoaded", () => {
  initSortControl();
  initSearchControl();
  initModalOutsideClose();

  initRegionModal({
    onApply: () => {
      updateSelectedRegionTextOnly();
      fetchFilteredCards();
    }
  });

  initFishModal({
    onApply: () => {
      updateSelectedFishText();
      fetchFilteredCards();
    }
  });

  initDateModal({
    onApply: () => {
      updateSelectedDateTextOnly();
      fetchFilteredCards();
    }
  });
});
