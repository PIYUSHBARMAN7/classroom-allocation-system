import '../../styles/mascot.css'

export default function ClassroomMascot() {
  return (
    <div className="classroom-mascot" aria-hidden="true">
      <span className="mascot-spark spark-one">✦</span>
      <span className="mascot-spark spark-two">✦</span>

      <svg viewBox="0 0 340 380" className="mascot-art">
        <ellipse
          className="mascot-ground"
          cx="170"
          cy="350"
          rx="95"
          ry="13"
          fill="#171717"
          opacity=".2"
        />

        <g className="mascot-body">
          {/* Legs */}
          <rect
            x="115" y="280" width="42" height="53" rx="12"
            fill="#fffdee" stroke="#171717" strokeWidth="5"
          />
          <rect
            x="183" y="280" width="42" height="53" rx="12"
            fill="#fffdee" stroke="#171717" strokeWidth="5"
          />

          {/* Shoes */}
          <rect
            x="103" y="318" width="57" height="22" rx="8"
            fill="#ffe228" stroke="#171717" strokeWidth="5"
          />
          <rect
            x="181" y="318" width="57" height="22" rx="8"
            fill="#ffe228" stroke="#171717" strokeWidth="5"
          />

          {/* Left arm */}
          <path
            d="M105 194 Q68 207 70 244"
            fill="none" stroke="#171717"
            strokeWidth="26" strokeLinecap="round"
          />
          <path
            d="M105 194 Q68 207 70 244"
            fill="none" stroke="#f2a4cc"
            strokeWidth="16" strokeLinecap="round"
          />

          {/* Waving arm */}
          <g className="mascot-wave">
            <path
              d="M235 194 Q285 180 280 135"
              fill="none" stroke="#171717"
              strokeWidth="26" strokeLinecap="round"
            />
            <path
              d="M235 194 Q285 180 280 135"
              fill="none" stroke="#f2a4cc"
              strokeWidth="16" strokeLinecap="round"
            />
            <rect
              x="264" y="104" width="32" height="39" rx="12"
              fill="#ffe228" stroke="#171717" strokeWidth="5"
            />
          </g>

          {/* Torso */}
          <rect
            x="100" y="175" width="140" height="120" rx="25"
            fill="#fffdee" stroke="#171717" strokeWidth="5"
          />

          {/* Timetable chest */}
          <rect
            x="121" y="203" width="98" height="65" rx="7"
            fill="#dce8ff" stroke="#171717" strokeWidth="4"
          />
          <path
            d="M121 222h98M151 222v46M185 222v46M121 245h98"
            fill="none" stroke="#171717" strokeWidth="3"
          />
          <rect x="155" y="227" width="25" height="13" rx="2" fill="#ffe228" />
          <rect x="190" y="249" width="23" height="13" rx="2" fill="#f2a4cc" />

          {/* Head */}
          <rect
            x="94" y="78" width="152" height="108" rx="30"
            fill="#ffe228" stroke="#171717" strokeWidth="5"
          />

          {/* Face panel */}
          <rect
            x="110" y="100" width="120" height="64" rx="20"
            fill="#171717"
          />
          <g className="mascot-eyes" fill="#fffdee">
            <ellipse cx="143" cy="125" rx="8" ry="11" />
            <ellipse cx="197" cy="125" rx="8" ry="11" />
          </g>
          <path
            d="M155 145 Q170 156 185 145"
            fill="none" stroke="#fffdee"
            strokeWidth="4" strokeLinecap="round"
          />

          {/* Graduation cap */}
          <path
            d="M127 68v24 Q170 109 213 92V68"
            fill="#f2a4cc" stroke="#171717"
            strokeWidth="5" strokeLinejoin="round"
          />
          <path
            d="M170 30 250 61 170 92 90 61Z"
            fill="#fffdee" stroke="#171717"
            strokeWidth="5" strokeLinejoin="round"
          />
          <path
            d="M170 61h67v40"
            fill="none" stroke="#171717"
            strokeWidth="4" strokeLinecap="round"
          />
          <circle cx="237" cy="106" r="7" fill="#f2a4cc" stroke="#171717" strokeWidth="3" />
        </g>
      </svg>

      <span className="mascot-sticker">MAKE ROOM FOR LEARNING.</span>
    </div>
  )
}