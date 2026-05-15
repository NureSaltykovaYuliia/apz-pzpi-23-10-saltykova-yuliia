import { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, useMapEvents } from 'react-leaflet';
import L from 'leaflet';

// Fix for default marker icons in React-Leaflet
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

let DefaultIcon = L.icon({
    iconUrl: markerIcon,
    shadowUrl: markerShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41]
});

L.Marker.prototype.options.icon = DefaultIcon;

function LocationMarker({ position, setPosition, onChange }) {
  useMapEvents({
    click(e) {
      const newPos = [e.latlng.lat, e.latlng.lng];
      setPosition(newPos);
      onChange(e.latlng.lat, e.latlng.lng);
    },
  });

  return position === null ? null : (
    <Marker position={position}></Marker>
  );
}

export default function LocationPicker({ lat, lng, onChange }) {
  const initialPos = lat && lng ? [parseFloat(lat), parseFloat(lng)] : [50.0015, 36.2304]; // Kharkiv
  const [position, setPosition] = useState(lat && lng ? initialPos : null);

  const containerStyle = {
    width: '100%',
    height: '300px',
    borderRadius: '4px',
    border: '2px solid #000',
    zIndex: 1
  };

  return (
    <div className="location-picker">
      <MapContainer 
        center={initialPos} 
        zoom={13} 
        style={containerStyle}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <LocationMarker position={position} setPosition={setPosition} onChange={onChange} />
      </MapContainer>
      <div style={{ marginTop: '8px', fontSize: '12px', color: '#666' }}>
        Click on the map to select a location (OpenStreetMap)
      </div>
    </div>
  );
}
