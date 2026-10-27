import { useEffect, useState } from 'react'

const API = 'http://localhost:8080/api'
async function api(path, options = {}) {
  const response = await fetch(`${API}${path}`, {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    ...options
  })
  const data = response.status === 204 ? null : await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(data.message || data.error || `Request failed (${response.status})`)
  return data
}

export default function App() {
  const [user, setUser] = useState(null)
  const [mode, setMode] = useState('login')
  const [notice, setNotice] = useState('')
  const [services, setServices] = useState([])
  const [requests, setRequests] = useState([])
  const [quotations, setQuotations] = useState([])
  const [busy, setBusy] = useState(false)

  async function refresh(current = user) {
    if (!current) return
    try {
      if (current.role === 'ADMIN') {
        const [r, q] = await Promise.all([api('/admin/work-requests'), api('/admin/quotations')])
        setRequests(r); setQuotations(q)
      } else {
        const [r, q] = await Promise.all([api('/customer/work-requests'), api('/customer/quotations')])
        setRequests(r); setQuotations(q)
      }
    } catch (e) { setNotice(e.message) }
  }

  useEffect(() => {
    api('/services').then(setServices).catch(() => {})
    api('/auth/me').then(async u => { setUser(u); await refresh(u) }).catch(() => {})
  }, [])

  async function handleAuth(e) {
    e.preventDefault(); setBusy(true); setNotice('')
    const form = new FormData(e.currentTarget)
    const body = Object.fromEntries(form.entries())
    try {
      if (mode === 'register') {
        await api('/auth/register', { method: 'POST', body: JSON.stringify(body) })
        setMode('login'); setNotice('Account created. Please log in.')
      } else {
        const u = await api('/auth/login', { method: 'POST', body: JSON.stringify(body) })
        setUser(u); await refresh(u); setNotice(`Welcome, ${u.fullName}!`)
      }
    } catch (err) { setNotice(err.message) } finally { setBusy(false) }
  }

  async function logout() {
    try { await api('/auth/logout', { method: 'POST' }) } catch {}
    setUser(null); setRequests([]); setQuotations([]); setNotice('You have logged out.')
  }

  async function submitRequest(e) {
    e.preventDefault(); setNotice('')
    const form = new FormData(e.currentTarget)
    const body = Object.fromEntries(form.entries())
    if (!body.requiredDate) delete body.requiredDate
    try {
      await api('/customer/work-requests', { method: 'POST', body: JSON.stringify(body) })
      e.currentTarget.reset()
      setNotice('Your work request was submitted successfully.')
      await refresh()
    } catch (err) { setNotice(err.message) }
  }

  async function createQuotation(e, requestId) {
    e.preventDefault(); setNotice('')
    const body = Object.fromEntries(new FormData(e.currentTarget).entries())
    body.totalAmount = Number(body.totalAmount)
    if (!body.validUntil) delete body.validUntil
    try {
      await api(`/admin/quotations/${requestId}`, { method: 'POST', body: JSON.stringify(body) })
      setNotice('Quotation created and sent to the customer.')
      await refresh()
    } catch (err) { setNotice(err.message) }
  }

  async function decision(id, value) {
    try {
      await api(`/customer/quotations/${id}/decision`, { method: 'PUT', body: JSON.stringify({ decision: value }) })
      setNotice(`Quotation ${value.toLowerCase()}.`); await refresh()
    } catch (err) { setNotice(err.message) }
  }

  return <div>
    <header className="topbar">
      <a className="brand" href="#"><span className="brand-icon">⚡</span><span>SPARK<span className="brand-light">LINE</span><small>ELECTRICAL SERVICES</small></span></a>
      <nav><a href="#services">Services</a><a href="#about">About</a><a href="#contact">Contact</a>{user && <button className="outline small" onClick={logout}>Log out</button>}</nav>
    </header>
    <main>
      <section className="hero">
        <div className="hero-copy"><div className="eyebrow">RELIABLE • SAFE • PROFESSIONAL</div><h1>Powering your world.<br/><span>Safely and reliably.</span></h1>
          <p>Trusted electrical installation, repair and maintenance for homes, shops and businesses.</p>
          <a className="primary link-button" href={user ? '#dashboard' : '#account'}>{user ? 'Open dashboard' : 'Request a quotation'} ↗</a>
          <div className="trust-row"><span>✓ Skilled workmanship</span><span>✓ Clear quotations</span><span>✓ Safety first</span></div>
        </div>
        <div className="hero-art"><div className="circle"></div><div className="bolt">ϟ</div><div className="art-card"><b>Electrical solutions</b><span>From planning to installation</span></div><div className="art-line"></div></div>
      </section>

      <section className="section" id="services"><div className="section-heading"><div><div className="eyebrow">WHAT WE DO</div><h2>Electrical services</h2></div><p>Practical, dependable electrical work tailored to your needs.</p></div>
        <div className="service-grid">{(services.length ? services : [
          {name:'House Wiring',description:'New wiring, rewiring and electrical fitting.'},
          {name:'Electrical Repairs',description:'Troubleshooting and repair work.'},
          {name:'Commercial Work',description:'Office, shop and commercial installations.'}
        ]).map((s,i)=><article className="service-card" key={s.name}><div className="service-icon">{['⌁','ϟ','▦','☼','✓'][i%5]}</div><h3>{s.name}</h3><p>{s.description}</p><a href="#account">Request this service →</a></article>)}</div>
      </section>

      <section className="section about" id="about"><div><div className="eyebrow">YOUR LOCAL ELECTRICAL CONTRACTOR</div><h2>Good work starts<br/>with a clear plan.</h2></div><p>Tell us what electrical work you need. We will review your requirements and prepare a quotation for your project. Use your customer account to keep your requests and quotations in one place.</p></section>

      <section className="section account-section" id="account"><div className="section-heading"><div><div className="eyebrow">CUSTOMER PORTAL</div><h2>{user ? `Welcome, ${user.fullName}` : 'Let’s get your project started'}</h2></div>{user && <span className="role-pill">{user.role}</span>}</div>
        {notice && <div className="notice" role="status">{notice}</div>}
        {!user ? <div className="auth-card"><div className="tabs"><button className={mode==='login'?'active':''} onClick={()=>setMode('login')}>Log in</button><button className={mode==='register'?'active':''} onClick={()=>setMode('register')}>Create account</button></div>
          <form onSubmit={handleAuth} className="form-grid">
            {mode==='register' && <label>Full name<input name="fullName" required maxLength="120" placeholder="Your full name"/></label>}
            <label>Email address<input name="email" type="email" required placeholder="you@example.com"/></label>
            {mode==='register' && <label>Mobile number<input name="phone" required maxLength="30" placeholder="+91..."/></label>}
            <label>Password<input name="password" type="password" required minLength="8" placeholder="At least 8 characters"/></label>
            <button className="primary full" disabled={busy}>{busy?'Please wait…':mode==='login'?'Log in securely':'Create customer account'}</button>
          </form><p className="muted">Your enquiries and quotations are available after you log in.</p></div>
        : <div id="dashboard" className="dashboard">
          {user.role==='CUSTOMER' ? <>
            <div className="dashboard-title"><h3>Submit a work request</h3><span>Tell us what you need</span></div>
            <form onSubmit={submitRequest} className="form-grid request-form">
              <label>Type of work<select name="workType" required defaultValue=""><option value="" disabled>Select a service</option>{services.map(s=><option key={s.name}>{s.name}</option>)}<option>Other electrical work</option></select></label>
              <label>Property type<select name="propertyType" defaultValue="Residential"><option>Residential</option><option>Commercial</option><option>Industrial</option><option>Other</option></select></label>
              <label className="wide">Work location / address<input name="address" maxLength="300" placeholder="Area, city and address"/></label>
              <label>Required date<input name="requiredDate" type="date"/></label>
              <label className="wide">Describe the work<textarea name="description" required maxLength="3000" rows="4" placeholder="Explain the work, approximate size and any special requirements"/></label>
              <button className="primary">Submit work request</button>
            </form>
            <div className="dashboard-title"><h3>Your requests</h3><span>{requests.length} total</span></div>
            <div className="table-wrap"><table><thead><tr><th>Request</th><th>Work type</th><th>Required date</th><th>Status</th></tr></thead><tbody>{requests.map(r=><tr key={r.id}><td>REQ-{r.id}</td><td>{r.workType}</td><td>{r.requiredDate||'Not specified'}</td><td><span className="status">{r.status}</span></td></tr>)}{!requests.length&&<tr><td colSpan="4" className="empty">Your submitted requests will appear here.</td></tr>}</tbody></table></div>
            <div className="dashboard-title"><h3>Your quotations</h3></div><div className="quote-grid">{quotations.map(q=><article className="quote-card" key={q.id}><div className="quote-top"><span>{q.quotationNumber}</span><span className="status">{q.status}</span></div><h3>{q.workType}</h3><div className="amount">₹{Number(q.totalAmount).toLocaleString('en-IN')}</div><p>Valid until: {q.validUntil||'Not specified'}</p><p>{q.terms}</p>{q.status==='SENT'&&<div className="button-row"><button className="primary" onClick={()=>decision(q.id,'ACCEPTED')}>Accept</button><button className="outline" onClick={()=>decision(q.id,'REJECTED')}>Reject</button></div>}</article>)}{!quotations.length&&<p className="muted">Your quotations will appear here when prepared.</p>}</div>
          </> : <>
            <div className="dashboard-title"><h3>Admin dashboard</h3><span>Customer enquiries and quotations</span></div>
            <div className="stats"><div><span>Total requests</span><b>{requests.length}</b></div><div><span>New requests</span><b>{requests.filter(r=>r.status==='NEW').length}</b></div><div><span>Quotations</span><b>{quotations.length}</b></div></div>
            <div className="table-wrap"><table><thead><tr><th>Request</th><th>Customer</th><th>Work details</th><th>Status</th></tr></thead><tbody>{requests.map(r=><tr key={r.id}><td>REQ-{r.id}</td><td><b>{r.customerName}</b><small>{r.customerEmail}</small><small>{r.customerPhone}</small></td><td><b>{r.workType}</b><small>{r.address}</small><small>{r.description}</small></td><td><span className="status">{r.status}</span></td></tr>)}{!requests.length&&<tr><td colSpan="4" className="empty">No customer requests yet.</td></tr>}</tbody></table></div>
            <div className="dashboard-title"><h3>Create a quotation</h3></div>
            <div className="quote-grid">{requests.filter(r=>!quotations.some(q=>q.requestId===r.id)).map(r=><form className="quote-card" key={r.id} onSubmit={e=>createQuotation(e,r.id)}><div className="quote-top"><span>REQ-{r.id}</span><span className="status">{r.status}</span></div><h3>{r.customerName} · {r.workType}</h3><p>{r.description}</p><label>Total amount (₹)<input name="totalAmount" type="number" min="0.01" step="0.01" required placeholder="e.g. 25000"/></label><label>Valid until<input name="validUntil" type="date"/></label><label>Terms and conditions<textarea name="terms" rows="3" placeholder="Quotation terms"/></label><button className="primary">Create and send quotation</button></form>)}{!requests.some(r=>!quotations.some(q=>q.requestId===r.id))&&<p className="muted">All current requests have a quotation, or no requests have been submitted.</p>}</div>
            <div className="dashboard-title"><h3>Quotations issued</h3></div><div className="table-wrap"><table><thead><tr><th>Quotation</th><th>Customer</th><th>Work</th><th>Amount</th><th>Status</th></tr></thead><tbody>{quotations.map(q=><tr key={q.id}><td>{q.quotationNumber}</td><td>{q.customerName}</td><td>{q.workType}</td><td>₹{Number(q.totalAmount).toLocaleString('en-IN')}</td><td><span className="status">{q.status}</span></td></tr>)}{!quotations.length&&<tr><td colSpan="5" className="empty">No quotations issued yet.</td></tr>}</tbody></table></div>
          </>}
        </div>}
      </section>
      <section className="contact-strip" id="contact"><div><div className="eyebrow">READY TO DISCUSS YOUR PROJECT?</div><h2>Let’s power your next project.</h2></div><a className="primary link-button" href="#account">Contact / Request a quote ↗</a></section>
    </main>
    <footer><a className="brand footer-brand" href="#"><span className="brand-icon">⚡</span><span>SPARK<span className="brand-light">LINE</span><small>ELECTRICAL SERVICES</small></span></a><p>Reliable electrical work. Clear communication. Safety first.</p><span>© {new Date().getFullYear()} Electrical Contractor Website</span></footer>
  </div>
}
