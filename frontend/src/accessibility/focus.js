export function initAccessibility(isFocusHighlightEnabled = () => true) {
  document.addEventListener('keydown', (event) => {
    if (event.key === 'Tab') {
      if (isFocusHighlightEnabled()) {
        document.body.classList.add('user-is-tabbing')
      }
    }
  })

  document.addEventListener('mousedown', () => {
    document.body.classList.remove('user-is-tabbing')
  })
}
