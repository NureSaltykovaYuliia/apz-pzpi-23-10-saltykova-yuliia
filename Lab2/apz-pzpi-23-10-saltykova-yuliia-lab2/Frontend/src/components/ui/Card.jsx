export default function Card({ children, color = '', className = '', ...props }) {
  const colorClass = color ? `card-${color}` : '';
  return (
    <div className={`card ${colorClass} ${className}`} {...props}>
      {children}
    </div>
  );
}
