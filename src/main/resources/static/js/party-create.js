let routeData = null;
let selectedVehicle = null;
let departure = null, destination = null;
let destinationLat = null, destinationLng = null;
let waypoints = [];

window.onload = function () {
  kakao.maps.load(() => {
    const map = new kakao.maps.Map(document.getElementById('map'), {
      center: new kakao.maps.LatLng(37.5665, 126.9780),
      level: 5
    });
    const geocoder = new kakao.maps.services.Geocoder();
    const places = new kakao.maps.services.Places();
    let polyline = null;
    let tempMarker = null;

    // ========== 주소 검색 ==========
    const searchBox = document.getElementById('addressSearch');
    const suggestions = document.getElementById('searchSuggestions');
    let debounceTimer;
    searchBox.addEventListener('input', () => {
      const keyword = searchBox.value.trim();
      clearTimeout(debounceTimer);
      if (keyword.length < 2) {
        suggestions.style.display = 'none';
        return;
      }
      debounceTimer = setTimeout(() => {
        places.keywordSearch(keyword, (data, status) => {
          if (status === kakao.maps.services.Status.OK) {
            suggestions.innerHTML = '';
            data.forEach(place => {
              const div = document.createElement('div');
              div.className = 'suggestion';
              div.textContent = place.place_name;
              div.onclick = () => {
                map.setCenter(new kakao.maps.LatLng(place.y, place.x));
                suggestions.style.display = 'none';
              };
              suggestions.appendChild(div);
            });
            suggestions.style.display = 'block';
          }
        });
      }, 300);
    });

    // ========== 지도 우클릭 마커 & 컨텍스트 메뉴 ==========
    kakao.maps.event.addListener(map, 'rightclick', function(mouseEvent) {
      const latlng = mouseEvent.latLng;
      const containerRect = document.getElementById('map').getBoundingClientRect();
      const menuX = containerRect.left + mouseEvent.point.x;
      const menuY = containerRect.top + mouseEvent.point.y;

      if (tempMarker) tempMarker.setMap(null);
      tempMarker = new kakao.maps.Marker({ position: latlng, map });

      const menu = document.getElementById('mapContextMenu');
      menu.innerHTML = `
        <button onclick="selectPoint('departure')">출발지</button>
        <button onclick="selectPoint('waypoint')">경유지</button>
        <button onclick="selectPoint('destination')">도착지</button>
      `;
      menu.style.left = `${menuX}px`;
      menu.style.top = `${menuY}px`;
      menu.style.display = 'block';

      window.selectPoint = function(type) {
        const position = tempMarker.getPosition();
        tempMarker.setMap(null);
        tempMarker = null;
        menu.style.display = 'none';

        if (type === 'departure') {
          if (departure) departure.setMap(null);
          departure = new kakao.maps.Marker({ position, map });
          geocoder.coord2Address(position.getLng(), position.getLat(), (result, status) => {
            if (status === kakao.maps.services.Status.OK) {
              document.getElementById('departurePoint').value = result[0].address.address_name;
              document.querySelector('input[name="departureLat"]').value = position.getLat();
              document.querySelector('input[name="departureLng"]').value = position.getLng();
              tryAutoFetchRoute();
              updateInfoCard();
            }
          });
        } else if (type === 'destination') {
          if (destination) destination.setMap(null);
          destination = new kakao.maps.Marker({ position, map });
          destinationLat = position.getLat();
          destinationLng = position.getLng();
          geocoder.coord2Address(position.getLng(), position.getLat(), (result, status) => {
            if (status === kakao.maps.services.Status.OK) {
              document.getElementById('destination').value = result[0].address.address_name;
              document.querySelector('input[name="destinationLat"]').value = position.getLat();
              document.querySelector('input[name="destinationLng"]').value = position.getLng();
              tryAutoFetchRoute();
              updateInfoCard();
            }
          });
        } else if (type === 'waypoint') {
          const marker = new kakao.maps.Marker({ position, map });
          geocoder.coord2Address(position.getLng(), position.getLat(), (result, status) => {
            if (status === kakao.maps.services.Status.OK) {
              document.getElementById('waypoint').value = result[0].address.address_name;
              updateInfoCard();
            }
          });
          waypoints.push(marker);
          kakao.maps.event.addListener(marker, 'click', () => {
            if (confirm('이 마커를 삭제할까요?')) {
              marker.setMap(null);
              waypoints = waypoints.filter(m => m !== marker);
              tryAutoFetchRoute();
              updateInfoCard();
            }
          });
          tryAutoFetchRoute();
          updateInfoCard();
        }
      };
    });

    // ========== 경로/예상정보/해양API 연동 ==========
    function tryAutoFetchRoute() {
      if (departure && destination) {
        fetch(`/api/route?startX=${departure.getPosition().getLng()}&startY=${departure.getPosition().getLat()}&endX=${destination.getPosition().getLng()}&endY=${destination.getPosition().getLat()}${waypoints.length ? '&waypoints=' + waypoints.map(m => m.getPosition().getLng() + ',' + m.getPosition().getLat()).join('_') : ''}`)
          .then(res => res.json())
          .then(data => {
            routeData = data;
            if (polyline) polyline.setMap(null);
            const route = data.routes[0];
            const path = route.path.map(p => new kakao.maps.LatLng(p[1], p[0]));
            polyline = new kakao.maps.Polyline({
              path,
              strokeWeight: 5,
              strokeColor: '#007BFF',
              strokeOpacity: 0.9,
              strokeStyle: 'solid'
            });
            polyline.setMap(map);

            // 예상 시간, 톨비
            const minutes = Math.round(data.duration / 60);
            const toll = data.toll || 0;
            document.getElementById('durationText').textContent = `약 ${minutes}분`;
            document.getElementById('tollText').textContent = `톨게이트 ${toll.toLocaleString()}원`;
            document.getElementById('routeInfoCard').style.display = 'flex';
            updateInfoCard();

            // 도착일 계산 후 해양정보 호출
            const depInput = document.getElementById('departure-date');
            if (depInput && depInput.value && destinationLat && destinationLng) {
              let [departureDate, departureTime] = depInput.value.split('T');
              const durationMinutes = Math.round(data.duration / 60);
              const depDateObj = new Date(`${departureDate}T${departureTime}:00`);
              depDateObj.setMinutes(depDateObj.getMinutes() + durationMinutes);
              const arrivalDate = depDateObj.toISOString().slice(0, 10);
              const arrivalTime = depDateObj.toTimeString().slice(0, 5);
              const triptype = document.getElementById("triptype").value;
              fetchMarineInfo(destinationLat, destinationLng, arrivalDate, arrivalTime, triptype);
            }

            // 연료비 계산(차량 선택 시)
            if (selectedVehicle) {
              calculateFuelCost(routeData, selectedVehicle).then(result => {
                if (result) {
                  document.getElementById("fuelText").innerHTML =
                    `🚘 ${document.getElementById("carSearchInput").value}<br>` +
                    `연비: ${selectedVehicle.cityEff}km/L(도심), ${selectedVehicle.highwayEff}km/L(고속) / 연료: ${selectedVehicle.fuelType}<br>` +
                    `<br>예상 사용: ${result.fuelUsed}L / 단가: ${result.fuelPrice.toLocaleString()}원/L<br>` +
                    `<strong>예상 연료비: ${result.fuelCost.toLocaleString()}원</strong>`;
                  updateInfoCard();
                }
              });
            }
          });
      }
    }

    // ========== 차량 검색 ==========
    const carInput = document.getElementById("carSearchInput");
    const suggestionBox = document.getElementById("carSuggestions");
    let carDebounce;
    carInput.addEventListener("input", () => {
      const keyword = carInput.value.trim();
      clearTimeout(carDebounce);
      if (keyword.length < 2) {
        suggestionBox.style.display = 'none';
        return;
      }
      carDebounce = setTimeout(() => {
        fetch(`/api/car/search?keyword=${encodeURIComponent(keyword)}`)
          .then(res => res.json())
          .then(data => {
            suggestionBox.innerHTML = "";
            data.forEach(fullModelName => {
              const div = document.createElement("div");
              div.textContent = fullModelName;
              div.classList.add("suggestion");
              div.onclick = () => selectCarModel(fullModelName);
              suggestionBox.appendChild(div);
            });
            suggestionBox.style.display = "block";
          });
      }, 300);
    });
    function selectCarModel(modelName) {
      carInput.value = modelName;
      suggestionBox.style.display = "none";
      fetch(`/api/car/model?name=${encodeURIComponent(modelName)}`)
        .then(res => res.json())
        .then(data => {
          selectedVehicle = {
            cityEff: data.cityEff,
            highwayEff: data.highwayEff,
            fuelType: data.fuelType
          };
          document.getElementById("fuelText").innerHTML =
            `🚘 ${modelName}<br>연비: ${data.cityEff}km/L(도심), ${data.highwayEff}km/L(고속) / 연료: ${data.fuelType}`;
          if (routeData) {
            calculateFuelCost(routeData, selectedVehicle).then(result => {
              if (result) {
                document.getElementById("fuelText").innerHTML +=
                  `<br><br>예상 사용: ${result.fuelUsed}L / 단가: ${result.fuelPrice.toLocaleString()}원/L<br>` +
                  `<strong>예상 연료비: ${result.fuelCost.toLocaleString()}원</strong>`;
              }
              updateInfoCard();
            });
          } else {
            updateInfoCard();
          }
        });
    }

    // ========== 카드뷰 실시간 갱신 ==========
    function updateInfoCard() {
      document.getElementById('departurePointView').textContent =
        document.getElementById('departurePoint').value || '-';
      document.getElementById('destinationView').textContent =
        document.getElementById('destination').value || '-';
      document.getElementById('waypointView').textContent =
        waypoints.length ? waypoints.map((_, i) => `경유지${i + 1}`).join(', ') : '-';
      document.getElementById('carInfoView').textContent =
        carInput.value || '-';
      document.getElementById('fuelInfoView').textContent =
        document.getElementById('fuelText')?.textContent?.replace(/\s+/g, ' ').trim() || '-';
      // 예상 소요정보
      const dur = document.getElementById('durationText').textContent;
      const toll = document.getElementById('tollText').textContent;
      const card = document.getElementById('routeInfoCard');
      if (dur || toll) {
        card.style.display = '';
        card.querySelector('#durationText').textContent = dur;
        card.querySelector('#tollText').textContent = toll;
      } else {
        card.style.display = 'none';
      }
    }

    // ======= 폼 제출(경유지 등 hidden input 값 세팅) =======
    document.getElementById('partyForm').addEventListener('submit', () => {
      const container = document.getElementById('waypointInputs');
      container.innerHTML = '';
      waypoints.forEach((marker, idx) => {
        const lat = marker.getPosition().getLat();
        const lng = marker.getPosition().getLng();
        container.innerHTML += `
          <input type="hidden" name="waypoints[${idx}].lat" value="${lat}" />
          <input type="hidden" name="waypoints[${idx}].lng" value="${lng}" />
          <input type="hidden" name="waypoints[${idx}].name" value="경유지${idx + 1}" />
        `;
      });
      if (departure) {
        document.querySelector('input[name="departureLat"]').value = departure.getPosition().getLat();
        document.querySelector('input[name="departureLng"]').value = departure.getPosition().getLng();
      }
      if (destination) {
        document.querySelector('input[name="destinationLat"]').value = destination.getPosition().getLat();
        document.querySelector('input[name="destinationLng"]').value = destination.getPosition().getLng();
      }
    });

    // ====== 입력값 바뀔 때마다 카드 갱신 ======
    ['departurePoint', 'destination', 'waypoint'].forEach(id => {
      document.getElementById(id).addEventListener('change', updateInfoCard);
    });
    carInput.addEventListener('change', updateInfoCard);

  });
}

// ======= 해양정보 및 연료비 계산은 기존 함수 활용 =======
function fetchMarineInfo(destinationLat, destinationLng, arrivalDate, arrivalTime, triptype) {
  // ... 기존과 동일
  fetch(`/api/marine?lat=${destinationLat}&lon=${destinationLng}&departureDate=${encodeURIComponent(arrivalDate)}&arrivalTime=${encodeURIComponent(arrivalTime)}`)
    .then(res => res.json())
    .then(data => {
      window.renderFishingInfo(data, arrivalDate, arrivalTime, triptype);
    });
}

async function calculateFuelCost(routeData, vehicleData) {
  const distanceKm = routeData.distance / 1000;
  const cityEff = vehicleData.cityEff;
  const highwayEff = vehicleData.highwayEff;
  const fuelType = vehicleData.fuelType;
  const fuelPrices = await fetch("/api/fuel-price").then(res => res.json()).catch(() => null);
  if (!fuelPrices || !fuelPrices[fuelType]) return null;
  const fuelPrice = fuelPrices[fuelType];
  const weightedEff = routeData.highwayRatio * highwayEff + routeData.generalRatio * cityEff;
  const usedFuel = distanceKm / weightedEff;
  const fuelCost = Math.round(usedFuel * fuelPrice);
  return {
    fuelUsed: usedFuel.toFixed(2),
    fuelCost: fuelCost,
    fuelType: fuelType,
    fuelPrice: fuelPrice
  };
}

