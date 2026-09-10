import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import App from './App'

describe('LedgerGuard dashboard', () => {
  it('renders wallet balances and recent activity', () => {
    render(<App />)
    expect(screen.getByRole('heading', { name: 'Overview' })).toBeInTheDocument()
    expect(screen.getByText('Operating wallet')).toBeInTheDocument()
    expect(screen.getByText('Client settlement')).toBeInTheDocument()
  })

  it('completes a valid transfer in demo mode', () => {
    render(<App />)
    fireEvent.click(screen.getByRole('button', { name: /new transfer/i }))
    fireEvent.change(screen.getByLabelText(/amount/i), { target: { value: '125' } })
    fireEvent.change(screen.getByLabelText(/reference/i), { target: { value: 'QA transfer' } })
    fireEvent.click(screen.getByRole('button', { name: /complete transfer/i }))
    expect(screen.getByRole('status')).toHaveTextContent('Transfer completed')
    expect(screen.getByText('QA transfer')).toBeInTheDocument()
  })

  it('blocks transfers that exceed the available balance', () => {
    render(<App />)
    fireEvent.click(screen.getByRole('button', { name: /new transfer/i }))
    fireEvent.change(screen.getByLabelText(/amount/i), { target: { value: '999999' } })
    expect(screen.getByText(/exceeds the available balance/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /complete transfer/i })).toBeDisabled()
  })
})
