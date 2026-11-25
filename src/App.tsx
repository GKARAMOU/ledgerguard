import { FormEvent, useCallback, useEffect, useMemo, useState } from 'react'
import {
  ArrowDownLeft,
  ArrowRight,
  ArrowUpRight,
  Check,
  ChevronDown,
  CircleDollarSign,
  Code2,
  CreditCard,
  Github,
  LayoutDashboard,
  Menu,
  RefreshCcw,
  Search,
  Send,
  ShieldCheck,
  WalletCards,
  X,
} from 'lucide-react'

type Wallet = {
  id: string
  name: string
  reference: string
  balance: number
  accent: 'mint' | 'blue' | 'amber'
}

type Transaction = {
  id: string
  type: 'credit' | 'debit' | 'transfer'
  title: string
  reference: string
  amount: number
  timestamp: string
  status: 'Completed' | 'Pending'
}

const initialWallets: Wallet[] = [
  { id: 'operating', name: 'Operating wallet', reference: 'EUR · 9842', balance: 12450.8, accent: 'mint' },
  { id: 'reserve', name: 'Reserve wallet', reference: 'EUR · 2105', balance: 8200, accent: 'blue' },
  { id: 'expenses', name: 'Expenses wallet', reference: 'EUR · 7710', balance: 3100.25, accent: 'amber' },
]

const initialTransactions: Transaction[] = [
  { id: 'TX-1048', type: 'credit', title: 'Client settlement', reference: 'Invoice #INV-2408', amount: 2450, timestamp: 'Today, 10:42', status: 'Completed' },
  { id: 'TX-1047', type: 'debit', title: 'Cloud infrastructure', reference: 'Monthly services', amount: -384.2, timestamp: 'Today, 09:18', status: 'Completed' },
  { id: 'TX-1046', type: 'transfer', title: 'Operating → Expenses', reference: 'Team budget', amount: -1200, timestamp: 'Yesterday, 16:04', status: 'Completed' },
  { id: 'TX-1045', type: 'credit', title: 'Marketplace payout', reference: 'Settlement #8291', amount: 780.5, timestamp: 'Yesterday, 12:36', status: 'Completed' },
]

const trend = [42, 56, 48, 70, 61, 86, 74, 91, 67, 82, 78, 96]

const formatCurrency = (value: number) =>
  new Intl.NumberFormat('en-IE', { style: 'currency', currency: 'EUR' }).format(value)

type ModelTool = {
  name: string
  title: string
  description: string
  inputSchema: object
  annotations: { readOnlyHint: boolean; untrustedContentHint: boolean }
  execute: (input: unknown) => unknown | Promise<unknown>
}

declare global {
  interface Document {
    modelContext?: {
      registerTool: (tool: ModelTool, options?: { signal?: AbortSignal }) => void | Promise<void>
    }
  }
}

function App() {
  const [wallets, setWallets] = useState(initialWallets)
  const [transactions, setTransactions] = useState(initialTransactions)
  const [mobileNavOpen, setMobileNavOpen] = useState(false)
  const [transferOpen, setTransferOpen] = useState(false)
  const [notice, setNotice] = useState('')
  const [query, setQuery] = useState('')
  const [fromId, setFromId] = useState(initialWallets[0].id)
  const [toId, setToId] = useState(initialWallets[1].id)
  const [amount, setAmount] = useState('')
  const [reference, setReference] = useState('')

  const totalBalance = useMemo(() => wallets.reduce((sum, wallet) => sum + wallet.balance, 0), [wallets])
  const fromWallet = wallets.find((wallet) => wallet.id === fromId) ?? wallets[0]
  const canTransfer = Number(amount) > 0 && Number(amount) <= fromWallet.balance && fromId !== toId

  const applyTransfer = useCallback((sourceId: string, destinationId: string, value: number, transferReference: string) => {
    const source = wallets.find((wallet) => wallet.id === sourceId)
    const destination = wallets.find((wallet) => wallet.id === destinationId)
    if (!source || !destination) throw new Error('Choose an existing source and destination wallet.')
    if (sourceId === destinationId) throw new Error('Source and destination wallets must be different.')
    if (!Number.isFinite(value) || value <= 0) throw new Error('Amount must be greater than zero.')
    if (value > source.balance) throw new Error('The amount exceeds the available balance.')

    setWallets((current) =>
      current.map((wallet) => {
        if (wallet.id === sourceId) return { ...wallet, balance: wallet.balance - value }
        if (wallet.id === destinationId) return { ...wallet, balance: wallet.balance + value }
        return wallet
      }),
    )
    setTransactions((current) => [
      {
        id: `TX-${1049 + current.length}`,
        type: 'transfer',
        title: `${source.name.replace(' wallet', '')} → ${destination.name.replace(' wallet', '')}`,
        reference: transferReference.trim() || 'Internal transfer',
        amount: -value,
        timestamp: 'Just now',
        status: 'Completed',
      },
      ...current,
    ])
    return {
      status: 'completed',
      sourceWalletId: sourceId,
      destinationWalletId: destinationId,
      amount: value,
      currency: 'EUR',
    }
  }, [wallets])

  const completeTransfer = (event: FormEvent) => {
    event.preventDefault()
    if (!canTransfer) return

    const value = Number(amount)
    const destination = wallets.find((wallet) => wallet.id === toId)!
    applyTransfer(fromId, toId, value, reference)
    setAmount('')
    setReference('')
    setTransferOpen(false)
    setNotice(`Transfer completed. ${formatCurrency(value)} moved to ${destination.name}.`)
    window.setTimeout(() => setNotice(''), 4200)
  }

  useEffect(() => {
    const context = document.modelContext
    if (!context?.registerTool) return

    const lifecycle = new AbortController()
    const registration = context.registerTool(
      {
        name: 'complete_wallet_transfer',
        title: 'Complete wallet transfer',
        description: 'Move a positive EUR amount between two demo wallets and update the visible ledger.',
        inputSchema: {
          type: 'object',
          properties: {
            sourceWalletId: { type: 'string', enum: wallets.map((wallet) => wallet.id) },
            destinationWalletId: { type: 'string', enum: wallets.map((wallet) => wallet.id) },
            amount: { type: 'number', exclusiveMinimum: 0 },
            reference: { type: 'string', maxLength: 60 },
          },
          required: ['sourceWalletId', 'destinationWalletId', 'amount'],
          additionalProperties: false,
        },
        annotations: { readOnlyHint: false, untrustedContentHint: false },
        execute(input) {
          if (!input || typeof input !== 'object') throw new Error('Transfer input must be an object.')
          const candidate = input as Record<string, unknown>
          if (typeof candidate.sourceWalletId !== 'string' || typeof candidate.destinationWalletId !== 'string' || typeof candidate.amount !== 'number') {
            throw new Error('sourceWalletId, destinationWalletId and amount are required.')
          }
          return applyTransfer(
            candidate.sourceWalletId,
            candidate.destinationWalletId,
            candidate.amount,
            typeof candidate.reference === 'string' ? candidate.reference : 'Automated transfer',
          )
        },
      },
      { signal: lifecycle.signal },
    )
    void Promise.resolve(registration).catch(() => undefined)
    return () => lifecycle.abort()
  }, [applyTransfer, wallets])

  const resetDemo = () => {
    setWallets(initialWallets)
    setTransactions(initialTransactions)
    setNotice('Demo data restored.')
    window.setTimeout(() => setNotice(''), 3000)
  }

  return (
    <div className="app-shell">
      <aside className={`sidebar ${mobileNavOpen ? 'sidebar-open' : ''}`}>
        <div className="brand">
          <div className="brand-mark"><ShieldCheck size={23} /></div>
          <span>LedgerGuard</span>
          <button className="icon-button close-nav" onClick={() => setMobileNavOpen(false)} aria-label="Close navigation"><X /></button>
        </div>

        <nav aria-label="Primary navigation">
          <a className="nav-item active" href="#overview"><LayoutDashboard /> Overview</a>
          <a className="nav-item" href="#wallets"><WalletCards /> Wallets <span className="nav-count">3</span></a>
          <a className="nav-item" href="#activity"><CreditCard /> Transactions</a>
          <a className="nav-item" href="https://github.com/GKARAMOU/ledgerguard" target="_blank" rel="noreferrer"><Code2 /> API &amp; docs</a>
        </nav>

        <div className="sidebar-spacer" />
        <div className="demo-panel">
          <span className="status-dot" />
          <div><strong>Browser demo</strong><small>Changes stay in this session</small></div>
        </div>
        <button className="nav-item reset-button" onClick={resetDemo}><RefreshCcw /> Reset data</button>
        <a className="nav-item" href="https://github.com/GKARAMOU/ledgerguard" target="_blank" rel="noreferrer"><Github /> View source</a>
        
      </aside>

      {mobileNavOpen && <button className="nav-backdrop" onClick={() => setMobileNavOpen(false)} aria-label="Close navigation" />}

      <main>
        <header className="topbar">
          <button className="icon-button menu-button" onClick={() => setMobileNavOpen(true)} aria-label="Open navigation"><Menu /></button>
          <div>
            <span className="eyebrow">FINANCE OPERATIONS</span>
            <h1>Overview</h1>
          </div>
          <div className="topbar-actions">
            <label className="search-box">
              <Search size={18} />
              <span className="sr-only">Search transactions</span>
              <input type="search" placeholder="Search transactions" value={query} onChange={(event) => setQuery(event.target.value)} />
              <kbd>⌘ K</kbd>
            </label>
            <div className="environment"><span /> Demo environment <ChevronDown size={15} /></div>
            <span className="avatar" aria-label="Demo account">GK</span>
          </div>
        </header>

        <div className="workspace" id="overview">
          <section className="balance-panel">
            <div className="balance-copy">
              <span className="section-label">Total available balance</span>
              <div className="balance-line">
                <strong>{formatCurrency(totalBalance)}</strong>
                <span className="change-positive"><ArrowUpRight size={15} /> 8.4%</span>
              </div>
              <span className="muted">Across 3 active EUR wallets</span>
            </div>
            <div className="chart" aria-label="Balance trend over the last twelve weeks">
              {trend.map((height, index) => <span key={index} style={{ height: `${height}%` }} />)}
              <div className="chart-baseline" />
            </div>
            <button className="primary-button" onClick={() => setTransferOpen(true)}><Send size={18} /> New transfer</button>
          </section>

          <section id="wallets" className="section-block">
            <div className="section-heading">
              <div><span className="section-label">ACTIVE ACCOUNTS</span><h2>Your wallets</h2></div>
              
            </div>
            <div className="wallet-grid">
              {wallets.map((wallet) => (
                <article className={`wallet-card accent-${wallet.accent}`} key={wallet.id}>
                  <div className="wallet-topline"><span className="wallet-icon"><CircleDollarSign size={19} /></span><span className="wallet-state"><Check size={12} /> Active</span></div>
                  <span className="wallet-name">{wallet.name}</span>
                  <strong>{formatCurrency(wallet.balance)}</strong>
                  <span className="wallet-reference">{wallet.reference}</span>
                </article>
              ))}
            </div>
          </section>

          <section id="activity" className="section-block activity-block">
            <div className="section-heading">
              <div><span className="section-label">AUDIT TRAIL</span><h2>Recent activity</h2></div>
              <span>Recent transactions</span>
            </div>
            <div className="transaction-list">
              <div className="transaction-row transaction-head"><span>Transaction</span><span>Reference</span><span>Date</span><span>Status</span><span>Amount</span></div>
              {transactions.filter((transaction) => `${transaction.title} ${transaction.reference} ${transaction.id}`.toLowerCase().includes(query.toLowerCase())).slice(0, 6).map((transaction) => (
                <div className="transaction-row" key={transaction.id}>
                  <div className="transaction-title">
                    <span className={`transaction-icon ${transaction.type}`}>
                      {transaction.type === 'credit' ? <ArrowDownLeft /> : transaction.type === 'debit' ? <ArrowUpRight /> : <ArrowRight />}
                    </span>
                    <div><strong>{transaction.title}</strong><small>{transaction.id}</small></div>
                  </div>
                  <span className="reference-cell">{transaction.reference}</span>
                  <span>{transaction.timestamp}</span>
                  <span><span className="completed-dot" />{transaction.status}</span>
                  <strong className={transaction.amount > 0 ? 'positive-amount' : ''}>{transaction.amount > 0 ? '+' : ''}{formatCurrency(transaction.amount)}</strong>
                </div>
              ))}
            </div>
          </section>
        </div>
      </main>

      {transferOpen && (
        <div className="modal-backdrop" role="presentation" onMouseDown={() => setTransferOpen(false)}>
          <section className="transfer-modal" role="dialog" aria-modal="true" aria-labelledby="transfer-title" onMouseDown={(event) => event.stopPropagation()}>
            <div className="modal-heading">
              <div><span className="section-label">SECURE INTERNAL TRANSFER</span><h2 id="transfer-title">Move funds</h2></div>
              <button className="icon-button" onClick={() => setTransferOpen(false)} aria-label="Close transfer"><X /></button>
            </div>
            <form onSubmit={completeTransfer}>
              <label>From wallet<select value={fromId} onChange={(event) => setFromId(event.target.value)}>{wallets.map((wallet) => <option key={wallet.id} value={wallet.id}>{wallet.name} — {formatCurrency(wallet.balance)}</option>)}</select></label>
              <label>To wallet<select value={toId} onChange={(event) => setToId(event.target.value)}>{wallets.map((wallet) => <option key={wallet.id} value={wallet.id}>{wallet.name}</option>)}</select></label>
              <label>Amount<div className="amount-input"><span>€</span><input type="number" min="0.01" step="0.01" inputMode="decimal" value={amount} onChange={(event) => setAmount(event.target.value)} placeholder="0.00" required /></div></label>
              <label>Reference<input type="text" maxLength={60} value={reference} onChange={(event) => setReference(event.target.value)} placeholder="e.g. Team budget" /></label>
              {fromId === toId && <p className="form-error">Choose two different wallets.</p>}
              {Number(amount) > fromWallet.balance && <p className="form-error">The amount exceeds the available balance.</p>}
              <div className="transfer-summary"><ShieldCheck size={18} /><span>The API version applies atomic updates, idempotency keys and balance validation.</span></div>
              <button className="primary-button submit-button" disabled={!canTransfer}><Send size={18} /> Complete transfer</button>
            </form>
          </section>
        </div>
      )}

      {notice && <div className="toast" role="status"><Check size={17} />{notice}</div>}
    </div>
  )
}

export default App
