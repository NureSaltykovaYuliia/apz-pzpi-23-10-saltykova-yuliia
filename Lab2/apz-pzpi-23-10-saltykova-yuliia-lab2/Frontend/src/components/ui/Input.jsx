export default function Input({ label, id, type = 'text', className = '', ...props }) {
  const inputId = id || label?.toLowerCase().replace(/\s+/g, '-');
  return (
    <div className={`input-group ${className}`}>
      {label && <label htmlFor={inputId}>{label}</label>}
      {type === 'textarea' ? (
        <textarea id={inputId} className="input-field" {...props} />
      ) : (
        <input id={inputId} type={type} className="input-field" {...props} />
      )}
    </div>
  );
}
