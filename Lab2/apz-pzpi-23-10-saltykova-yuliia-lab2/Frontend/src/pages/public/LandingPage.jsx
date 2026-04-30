import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import './LandingPage.css';

export default function LandingPage() {
  const { t } = useTranslation();

  return (
    <main className="landing-main">
      {/* 1. Hero Section */}
      <section className="landing-hero">
        <div className="hero-content">
          <div className="sticker sticker-green" style={{ transform: 'rotate(-2deg)' }}>
            {t('landing.badge')}
          </div>
          <h1 className="text-headline-xl">
            {t('landing.title')}
          </h1>
          <p className="hero-description brutal-border brutal-shadow-sm">
            {t('landing.description')}
          </p>
          <div className="hero-actions">
            <Link to="/register">
              <button className="btn btn-primary btn-lg">{t('landing.createProfile')}</button>
            </Link>
            <Link to="/register">
              <button className="btn btn-danger btn-lg">{t('landing.findFriends')}</button>
            </Link>
          </div>
        </div>
        <div className="hero-image-wrapper">
          <div className="hero-image-shadow"></div>
          <img
            className="hero-image"
            src="https://lh3.googleusercontent.com/aida-public/AB6AXuDob0Lq51wF-YYFr38BJ-oBm1bMOad_H4IwvNQqSyX00HGT7Y4yl5dR941nEoh-znQX8zkLrSnZN7jPIGkhqVHDGleEnqBMM8Hps-37DgmgTVbtlBpuegSGmclFPY8NpGbKPbXCxdpHZAmVUaHTa8XGSD0ib-oTevXfHgbl9ap8oyzKfRUm5vDTtdQJpHr6Bs1opxjmrqkBk6l6MijhF8n2d2uVeM6kPh2n4s4VT89IxiIGEjaUjZH9Y4LyOptarAq77PU_5TICrA0"
            alt="Dog with tactical harness"
          />
          <div className="hero-badge" style={{ transform: 'rotate(3deg)' }}>
            {t('landing.noRules')}
          </div>
        </div>
      </section>

      {/* 2. How It Works */}
      <section id="how-it-works" className="landing-section">
        <h2 className="section-title">{t('landing.howItWorks')}</h2>
        <div className="grid-cols-3">
          {/* Card 1 */}
          <div className="card card-red landing-card">
            <div className="landing-card-sticker" style={{ transform: 'rotate(6deg)', background: 'var(--color-primary)' }}>
              {t('landing.fast')}
            </div>
            <span className="material-symbols-outlined filled landing-card-icon">account_circle</span>
            <h3 className="text-headline-md">{t('landing.step1Title')}</h3>
            <p className="text-body-md">{t('landing.step1Desc')}</p>
          </div>
          {/* Card 2 */}
          <div className="card card-yellow landing-card">
            <div className="landing-card-sticker" style={{ transform: 'rotate(-6deg)', background: 'var(--color-secondary)' }}>
              {t('landing.interaction')}
            </div>
            <span className="material-symbols-outlined filled landing-card-icon">groups</span>
            <h3 className="text-headline-md">{t('landing.step2Title')}</h3>
            <p className="text-body-md">{t('landing.step2Desc')}</p>
          </div>
          {/* Card 3 */}
          <div className="card card-green landing-card">
            <div className="landing-card-sticker" style={{ transform: 'rotate(3deg)', background: 'var(--color-tertiary)' }}>
              {t('landing.fun')}
            </div>
            <span className="material-symbols-outlined filled landing-card-icon">celebration</span>
            <h3 className="text-headline-md">{t('landing.step3Title')}</h3>
            <p className="text-body-md">{t('landing.step3Desc')}</p>
          </div>
        </div>
      </section>

      {/* 3. Benefits */}
      <section className="landing-section">
        <h2 className="section-title">{t('landing.benefits')}</h2>
        <div className="grid-cols-2">
          <div className="card card-red" style={{ padding: 'var(--space-xl)', boxShadow: 'var(--shadow-xl)' }}>
            <span className="material-symbols-outlined filled" style={{ fontSize: 80, color: 'var(--color-black)' }}>verified_user</span>
            <h3 className="text-headline-lg" style={{ margin: 'var(--space-md) 0' }}>{t('landing.securityTitle')}</h3>
            <p className="text-body-lg">{t('landing.securityDesc')}</p>
          </div>
          <div className="card card-green" style={{ padding: 'var(--space-xl)', boxShadow: 'var(--shadow-xl)' }}>
            <span className="material-symbols-outlined filled" style={{ fontSize: 80, color: 'var(--color-black)' }}>tune</span>
            <h3 className="text-headline-lg" style={{ margin: 'var(--space-md) 0' }}>{t('landing.personalizationTitle')}</h3>
            <p className="text-body-lg">{t('landing.personalizationDesc')}</p>
          </div>
        </div>
      </section>

      {/* 4. Partner Services */}
      <section className="landing-section">
        <div className="flex-between" style={{ borderBottom: '8px solid var(--color-black)', paddingBottom: 'var(--space-md)' }}>
          <h2 className="text-headline-lg">{t('landing.partnerServices')}</h2>
          <Link to="/partners">
            <button className="btn btn-outline btn-sm">{t('landing.allServices')}</button>
          </Link>
        </div>
        <div className="grid-cols-4" style={{ marginTop: 'var(--space-xl)' }}>
          {[
            { icon: 'local_hospital', label: t('landing.veterinary'), hoverBg: 'var(--color-primary)' },
            { icon: 'content_cut', label: t('landing.grooming'), hoverBg: 'var(--color-tertiary)' },
            { icon: 'school', label: t('landing.training'), hoverBg: 'var(--color-secondary)' },
            { icon: 'storefront', label: t('landing.shops'), hoverBg: 'var(--color-primary)' },
          ].map((svc) => (
            <div key={svc.icon} className="partner-card card">
              <div className="partner-icon">
                <span className="material-symbols-outlined" style={{ fontSize: 36 }}>{svc.icon}</span>
              </div>
              <h4 className="text-headline-md">{svc.label}</h4>
            </div>
          ))}
        </div>
      </section>

      {/* 5. Tracking & Safety */}
      <section id="tracking" className="tracking-section brutal-border brutal-shadow-xl">
        <div className="tracking-content">
          <div className="tracking-text">
            <div className="tracking-badge">{t('landing.iotTech')}</div>
            <h2 className="text-headline-xl">{t('landing.trackingTitle')}</h2>
            <p className="tracking-desc">{t('landing.trackingDesc')}</p>
            <button className="btn btn-dark btn-lg" style={{ boxShadow: '8px 8px 0px 0px var(--color-primary)' }}>
              {t('landing.learnMore')}
            </button>
          </div>
          <div className="tracking-visual">
            <div className="tracking-circle animate-float">
              <span className="material-symbols-outlined filled" style={{ fontSize: 96, color: 'var(--color-red-500)' }}>pets</span>
              <div className="tracking-ring ring-1"></div>
              <div className="tracking-ring ring-2"></div>
            </div>
          </div>
        </div>
      </section>

      {/* 6. Testimonials */}
      <section id="testimonials" className="landing-section">
        <h2 className="section-title" style={{ textAlign: 'center', display: 'block' }}>{t('landing.testimonials')}</h2>
        <div className="grid-cols-3">
          {[
    { text: t('landing.testimonial1'), author: t('landing.testimonial1Author'), img: '/images/dog1.png', color: 'var(--color-tertiary)', shadow: 'var(--shadow-red)' },
    { text: t('landing.testimonial2'), author: t('landing.testimonial2Author'), img: '/images/dog2.png', color: 'var(--color-primary)', shadow: 'var(--shadow-yellow)', textColor: 'white' },
    { text: t('landing.testimonial3'), author: t('landing.testimonial3Author'), img: '/images/dog3.png', color: 'var(--color-secondary)', shadow: 'var(--shadow-green)' },
  ].map((item, i) => (
    <div key={i} className="card" style={{ boxShadow: item.shadow }}>
      <span className="material-symbols-outlined filled testimonial-quote" style={{ color: item.color }}>format_quote</span>
      <p className="text-body-md" style={{ paddingTop: 'var(--space-md)', marginBottom: 'var(--space-lg)' }}>
        {item.text}
      </p>
      <div className="testimonial-author">
        <div
          className="testimonial-avatar"
          style={{ 
            background: item.color, 
            color: item.textColor || 'var(--color-black)',
            overflow: 'hidden',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}
        >
          <img src={item.img} alt={item.author} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        </div>
                <p className="text-label">{item.author}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* 7. CTA */}
      <section className="cta-section brutal-border brutal-shadow-2xl">
        <div className="cta-deco cta-deco-woof">WOOF!</div>
        <div className="cta-deco cta-deco-bark">BARK!</div>
        <h2 className="text-headline-xl">{t('landing.ctaTitle')}</h2>
        <p className="cta-desc brutal-border">
          {t('landing.ctaDesc')}
        </p>
        <Link to="/register">
          <button className="btn btn-success btn-lg" style={{ fontSize: 'var(--fs-2xl)', padding: 'var(--space-lg) var(--space-2xl)' }}>
            {t('landing.joinPack')}
          </button>
        </Link>
      </section>
    </main>
  );
}
