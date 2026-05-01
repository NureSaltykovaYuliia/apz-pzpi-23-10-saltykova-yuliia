export default function Input({ label, id, error, type = 'text', className = '', ...props }) {
  const inputId = id || label?.toLowerCase().replace(/\s+/g, '-');
  return (
    <div className={`input-group ${className} ${error ? 'has-error' : ''}`}>
      {label && <label htmlFor={inputId}>{label}</label>}
      {type === 'textarea' ? (
        <textarea id={inputId} className="input-field" {...props} />
      ) : (
        <input id={inputId} type={type} className="input-field" {...props} />
      )}
      {error && <span className="input-error-msg" style={{ color: '#d32f2f', fontSize: '12px', marginTop: '4px' }}>{error}</span>}
    </div>
  );
}
