export function initAccessibility() {
  document.addEventListener('keydown', (event) => {
    if (event.key === 'Tab') {
      document.body.classList.add('user-is-tabbing')
    }
  })

  document.addEventListener('mousedown', () => {
    document.body.classList.remove('user-is-tabbing')
  })
}
