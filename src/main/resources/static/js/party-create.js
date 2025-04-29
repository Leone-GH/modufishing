kakao.maps.load(() => {
  const map = new kakao.maps.Map(document.getElementById('map'), {
    center: new kakao.maps.LatLng(37.5665, 126.9780),
    level: 5
  });

  const geocoder = new kakao.maps.services.Geocoder();
  let markers = [];
  let departureMarker = null;
  let destinationMarker = null;

  kakao.maps.event.addListener(map, 'rightclick', function(mouseEvent) {
    const latlng = mouseEvent.latLng;
    const menu = document.createElement('div');
    menu.innerHTML = `
      <div style="position:absolute;left:${mouseEvent.point.x}px;top:${mouseEvent.point.y}px;z-index:10;background:white;border:1px solid black;">
        <button onclick="selectPoint('departure', ${latlng.getLat()}, ${latlng.getLng()})">출발지</button>
        <button onclick="selectPoint('waypoint', ${latlng.getLat()}, ${latlng.getLng()})">경유지</button>
        <button onclick="selectPoint('destination', ${latlng.getLat()}, ${latlng.getLng()})">목적지</button>
      </div>
    `;
    document.body.appendChild(menu);

    setTimeout(() => document.body.removeChild(menu), 3000);
  });

  window.selectPoint = function(type, lat, lng) {
    if (type === 'departure') {
      if (departureMarker) departureMarker.setMap(null);
      departureMarker = new kakao.maps.Marker({ position: new kakao.maps.LatLng(lat, lng), map });
      geocoder.coord2Address(lng, lat, function(result, status) {
        if (status === kakao.maps.services.Status.OK) {
          document.getElementById('departurePoint').value = result[0].address.address_name;
        }
      });
    } else if (type === 'destination') {
      if (destinationMarker) destinationMarker.setMap(null);
      destinationMarker = new kakao.maps.Marker({ position: new kakao.maps.LatLng(lat, lng), map });
      geocoder.coord2Address(lng, lat, function(result, status) {
        if (status === kakao.maps.services.Status.OK) {
          document.getElementById('destination').value = result[0].address.address_name;
        }
      });
    } else if (type === 'waypoint') {
      const marker = new kakao.maps.Marker({ position: new kakao.maps.LatLng(lat, lng), map });
      markers.push(marker);
    }
  };

  document.getElementById('openRouteSelect').addEventListener('click', function() {
    document.getElementById('routeModal').style.display = 'block';
    loadRouteOptions();
  });

  document.getElementById('closeRouteModal').addEventListener('click', function() {
    document.getElementById('routeModal').style.display = 'none';
  });

  function loadRouteOptions() {
    const routeOptions = document.getElementById('routeOptions');
    routeOptions.innerHTML = '';

    const sampleRoutes = [
      { id: 1, title: '추천 경로', cost: '10,000원', time: '30분' },
      { id: 2, title: '최단 거리', cost: '8,000원', time: '28분' },
      { id: 3, title: '무료 우선', cost: '0원', time: '35분' }
    ];

    sampleRoutes.forEach(route => {
      const card = document.createElement('div');
      card.style.border = '1px solid gray';
      card.style.margin = '10px';
      card.style.padding = '10px';
      card.style.cursor = 'pointer';

      card.innerHTML = `
        <h3>${route.title}</h3>
        <p>예상 비용: ${route.cost}</p>
        <p>예상 시간: ${route.time}</p>
      `;

      card.onclick = () => selectRoute(route);
      routeOptions.appendChild(card);
    });
  }

  function selectRoute(route) {
    alert(`${route.title} 선택됨!\n비용: ${route.cost}, 시간: ${route.time}`);
    document.getElementById('routeModal').style.display = 'none';
  }
});
