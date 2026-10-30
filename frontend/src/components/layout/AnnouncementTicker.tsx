import '../../styles/ticker.css'

const messages = [
  'SMARTER CLASSROOM PLANNING',
  'FIND THE RIGHT ROOM',
  'CHECK TIMETABLE CONFLICTS',
  'PLAN • REVIEW • PUBLISH',
  'ONE CAMPUS. ONE CLEAR SCHEDULE.',
]

export default function AnnouncementTicker() {
  return (
    <div className="announcement-ticker">
      <p className="ticker-description">
        {messages.join('. ')}
      </p>

      <div className="ticker-window" aria-hidden="true">
        <div className="ticker-track">
          {[0, 1].map((copy) => (
            <div className="ticker-group" key={copy}>
              {messages.map((message) => (
                <span className="ticker-item" key={message}>
                  <span className="ticker-dot">✦</span>
                  {message}
                </span>
              ))}
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}