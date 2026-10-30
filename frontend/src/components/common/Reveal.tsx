import { useEffect, useRef } from 'react'
import type { ReactNode } from 'react'

type RevealProps = {
  children: ReactNode
  delay?: number
}

export default function Reveal({
  children,
  delay = 0,
}: RevealProps) {
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const element = ref.current

    if (!element || !('IntersectionObserver' in window)) {
      return
    }

    const motionPreference = window.matchMedia(
      '(prefers-reduced-motion: reduce)',
    )

    let animation: Animation | undefined

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (!entry.isIntersecting) return

        if (!motionPreference.matches) {
          animation = element.animate(
            [
              { opacity: 0, transform: 'translateY(20px)' },
              { opacity: 1, transform: 'translateY(0)' },
            ],
            {
              duration: 500,
              delay,
              easing: 'ease-out',
              fill: 'backwards',
            },
          )
        }

        observer.disconnect()
      },
      { threshold: 0.12 },
    )

    function stopMotion() {
      if (motionPreference.matches) animation?.cancel()
    }

    observer.observe(element)
    motionPreference.addEventListener('change', stopMotion)

    return () => {
      observer.disconnect()
      animation?.cancel()
      motionPreference.removeEventListener('change', stopMotion)
    }
  }, [delay])

  return (
    <div ref={ref} className="scroll-reveal">
      {children}
    </div>
  )
}