import { useEffect, useState } from 'react'
import {
    DEFAULT_DISPLAY_OPTIONS,
    formatCurrency,
    formatMonthDay,
    formatWeekday,
} from '../utils/formatters'

const DEFAULT_TABLE_HEIGHT = 480
const MIN_TABLE_HEIGHT = 320
const MAX_TABLE_HEIGHT = 1200

function clampTableHeight(height) {
    return Math.min(
        MAX_TABLE_HEIGHT,
        Math.max(MIN_TABLE_HEIGHT, height),
    )
}

function getDayOfWeek(dateString) {
    return new Date(`${dateString}T00:00:00`).getDay()
}

function getAllDates(planDetails) {
    return [
        ...new Set(
            Object.values(planDetails)
                .flatMap((planDetail) => planDetail.days ?? [])
                .map((day) => day.date),
        ),
    ].sort()
}

function getPositionsForDate(planDetail, date) {
    const day = planDetail?.days?.find((planDay) => planDay.date === date)

    return [...(day?.positions ?? [])].sort(
        (first, second) => first.positionOrder - second.positionOrder,
    )
}

function BlockCard({ position }) {
    const { block } = position
    const isTransfer = block.blockType === 'transfer'

    return (
        <article
            className={`comparison-block ${
                isTransfer
                    ? 'comparison-block-transfer'
                    : 'comparison-block-activity'
            }`}
        >
            <strong className="comparison-block-title">{block.blockName}</strong>

            <div className="comparison-block-meta">
                {isTransfer ? (
                    <>
                        {block.transferMethod && <span>{block.transferMethod}</span>}
                        <span className="comparison-place">
                            出発：{block.transferDeparture || '未設定'} ／ 到着：
                            {block.transferArrival || '未設定'}
                        </span>
                    </>
                ) : (
                    <>
                        {block.activityType && <span>{block.activityType}</span>}
                        {block.blockPlace && (
                            <span className="comparison-place">
                                場所：{block.blockPlace}
                            </span>
                        )}
                    </>
                )}

                {block.incompleteTodoCount > 0 && (
                    <span className="comparison-todo-count">
                        □ TODO {block.incompleteTodoCount}件
                    </span>
                )}
            </div>
        </article>
    )
}

export default function PlanComparisonTable({
    plans,
    planDetails,
    selectedPlanId,
    onSelectPlan,
    displayOptions = DEFAULT_DISPLAY_OPTIONS,
    isTodoDrawerOpen = false,
}) {
    const [tableHeight, setTableHeight] = useState(DEFAULT_TABLE_HEIGHT)
    const [resizeStart, setResizeStart] = useState(null)

    const dates = getAllDates(planDetails)

    /*
    プラン一覧表の高さ：
        下へドラッグ → 比較表を高くする
        上へドラッグ → 比較表を低くする
     */
    function handleResizePointerDown(event) {
        event.preventDefault()

        setResizeStart({
            pointerId: event.pointerId,
            startHeight: tableHeight,
            startY: event.clientY,
        })
    }

    useEffect(() => {
        if (!resizeStart) {
            return undefined
        }

        function handleWindowPointerMove(event) {
            if (event.pointerId !== resizeStart.pointerId) {
                return
            }

            const verticalDifference = event.clientY - resizeStart.startY

            setTableHeight(
                clampTableHeight(resizeStart.startHeight + verticalDifference),
            )
        }

        function handleWindowPointerUp(event) {
            if (event.pointerId === resizeStart.pointerId) {
                setResizeStart(null)
            }
        }

        window.addEventListener('pointermove', handleWindowPointerMove)
        window.addEventListener('pointerup', handleWindowPointerUp)
        window.addEventListener('pointercancel', handleWindowPointerUp)

        return () => {
            window.removeEventListener('pointermove', handleWindowPointerMove)
            window.removeEventListener('pointerup', handleWindowPointerUp)
            window.removeEventListener('pointercancel', handleWindowPointerUp)
        }
    }, [resizeStart])


    return (
        <section
            className={`comparison-table-wrapper ${
                isTodoDrawerOpen
                    ? 'comparison-table-wrapper-drawer-open'
                    : ''
            }`}
            style={{ height: `${tableHeight}px` }}
            aria-label="プラン比較表"
        >
            <div className="comparison-table-scroll-area">
                <div
                    className="comparison-table"
                    style={{
                        gridTemplateColumns: `120px repeat(${plans.length}, 240px)`,
                    }}
                >
                    <div className="comparison-corner" />

                    {plans.map((plan) => (
                        <button
                            key={plan.planId}
                            className={`comparison-plan-header ${
                                selectedPlanId === plan.planId
                                    ? 'comparison-plan-header-selected'
                                    : ''
                            }`}
                            type="button"
                            title={plan.planName}
                            aria-pressed={selectedPlanId === plan.planId}
                            onClick={() => onSelectPlan(plan.planId)}
                        >
                            <strong>{plan.planName}</strong>
                            <span>
                            費用：
                                {formatCurrency(plan.totalCost, displayOptions)}
                                {' ｜ '}
                                {plan.dayCount}日間
                        </span>
                        </button>
                    ))}

                    {dates.map((date) => (
                        <DateRow
                            key={date}
                            date={date}
                            plans={plans}
                            planDetails={planDetails}
                            displayOptions={displayOptions}
                        />
                    ))}
                </div>
            </div>

            <div
                className="comparison-resize-handle"
                aria-hidden="true"
                title="ドラッグして一覧表の高さを変更"
                onPointerDown={handleResizePointerDown}
            >
                <span />
                <span />
                <span />
            </div>
        </section>
    )

}

function DateRow({ date, plans, planDetails, displayOptions }) {
    const dayOfWeek = getDayOfWeek(date)
    const weekday = formatWeekday(date, displayOptions)
    const isSaturday = dayOfWeek === 6
    const isSunday = dayOfWeek === 0

    return (
        <>
            <div className="comparison-date">
                <strong
                    className={
                        isSaturday
                            ? 'comparison-date-saturday'
                            : isSunday
                              ? 'comparison-date-sunday'
                              : ''
                    }
                >
                    {formatMonthDay(date, displayOptions)}
                </strong>
                <span>{weekday}</span>
            </div>

            {plans.map((plan) => {
                const positions = getPositionsForDate(
                    planDetails[plan.planId],
                    date,
                )

                return (
                    <div key={plan.planId} className="comparison-cell">
                        <div className="comparison-block-stack">
                            {positions.map((position) => (
                                <BlockCard
                                    key={position.positionId}
                                    position={position}
                                />
                            ))}
                        </div>
                    </div>
                )
            })}
        </>
    )
}