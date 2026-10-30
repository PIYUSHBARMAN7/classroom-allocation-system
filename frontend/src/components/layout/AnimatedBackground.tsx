
import '../../styles/background.css'

function CalendarIcon() {
  return (
    <svg
      viewBox="0 0 48 48"
      fill="none"
      stroke="currentColor"
      strokeWidth="2.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <rect x="7" y="10" width="34" height="31" rx="3" />
      <path d="M7 20h34M16 6v9M32 6v9" />
      <path d="M15 27h4m10 0h4m-18 7h4m10 0h4" />
    </svg>
  )
}

function ClassroomIcon() {
  return (
    <svg
      viewBox="0 0 48 48"
      fill="none"
      stroke="currentColor"
      strokeWidth="2.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <rect x="7" y="8" width="34" height="23" rx="2" />
      <path d="M15 16h18M15 23h10M12 36h24M16 36v6M32 36v6" />
    </svg>
  )
}

export default function AnimatedBackground() {

  return (
    <>
      <div
        className="campus-background"
        aria-hidden="true"
      >
        <div className="campus-grid" />

        <div className="colour-wash wash-yellow" />
        <div className="colour-wash wash-blue" />
        <div className="colour-wash wash-pink" />

        <span className="floating-symbol symbol-one">
          <CalendarIcon />
        </span>

        <span className="floating-symbol symbol-two">
          <ClassroomIcon />
        </span>

        <span className="floating-symbol symbol-three">
          <CalendarIcon />
        </span>

        <span className="floating-symbol symbol-four">
          <ClassroomIcon />
        </span>
      </div>
    </>
  )
}