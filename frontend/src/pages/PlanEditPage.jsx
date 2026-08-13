import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ApiError } from '../api/client'
import { getPlan, getPlanTodos } from '../api/plans'
import PlanTodoPanel from "../components/PlanTodoPanel.jsx";
import {
    DEFAULT_DISPLAY_OPTIONS,
    formatMonthDay,
    formatWeekday,
} from '../utils/formatters'

const DISPLAY_OPTIONS = DEFAULT_DISPLAY_OPTIONS

function formatPlanPeriod(planDetail) {
    return `${new Intl.DateTimeFormat('ja-JP', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
    }).format(new Date(`${planDetail.planStartDate}T00:00:00`))} 〜 ${new Intl.DateTimeFormat(
        'ja-JP',
        {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
        },
    ).format(new Date(`${planDetail.planEndDate}T00:00:00`))}`
}

function ItineraryBlockCard({ position }) {
    const { block } = position
    const isTransfer = block.blockType === 'transfer'

    return (
        <article
            className={`itinerary-block ${
                isTransfer
                    ? 'itinerary-block-transfer'
                    : 'itinerary-block-activity'
            }`}
        >
            <strong className="itinerary-block-title">{block.blockName}</strong>

            <div className="itinerary-block-meta">
                {isTransfer ? (
                    <>
                        {block.transferMethod && (
                            <span>{block.transferMethod}</span>
                        )}
                        <span className="itinerary-block-place">
                            出発：{block.transferDeparture || '未設定'} ／ 到着：
                            {block.transferArrival || '未設定'}
                        </span>
                    </>
                ) : (
                    <>
                        {block.activityType && <span>{block.activityType}</span>}
                        {block.blockPlace && (
                            <span className="itinerary-block-place">
                                場所：{block.blockPlace}
                            </span>
                        )}
                    </>
                )}

                {block.incompleteTodoCount > 0 && (
                    <span className="itinerary-block-todo">
                        □ 未完TODO {block.incompleteTodoCount}件
                    </span>
                )}
            </div>
        </article>
    )
}

function PlanDayRow({ day }) {
    const sortedPositions = useMemo(
        () =>
            [...day.positions].sort(
                (first, second) =>
                    first.positionOrder - second.positionOrder,
            ),
        [day.positions],
    )

    return (
        <section className="itinerary-day">
            <div className="itinerary-day-date">
                <strong>{formatMonthDay(day.date, DISPLAY_OPTIONS)}</strong>
                <span>{formatWeekday(day.date, DISPLAY_OPTIONS)}</span>
            </div>

            <div className="itinerary-day-content">
                {sortedPositions.map((position) => (
                    <ItineraryBlockCard
                        key={position.positionId}
                        position={position}
                    />
                ))}
            </div>
        </section>
    )
}

export default function PlanEditPage() {
    const navigate = useNavigate()
    const { planId } = useParams()

    const [planDetail, setPlanDetail] = useState(null)
    const [isLoading, setIsLoading] = useState(true)
    const [errorType, setErrorType] = useState(null)

    const [todos, setTodos] = useState([])
    const [isTodoLoading, setIsTodoLoading] = useState(false)
    const [todoErrorMessage, setTodoErrorMessage] = useState('')
    const [isTodoPanelOpen, setIsTodoPanelOpen] = useState(true)

    const numericPlanId = Number(planId)

    useEffect(() => {
        let isMounted = true

        async function loadPlanDetail() {
            if (!Number.isInteger(numericPlanId) || numericPlanId < 1) {
                if (isMounted) {
                    setErrorType('not-found')
                    setIsLoading(false)
                }
                return
            }

            try {
                setIsLoading(true)
                setErrorType(null)

                const response = await getPlan(numericPlanId)

                if (isMounted) {
                    setPlanDetail(response)
                }
            } catch (error) {
                console.error('Failed to load plan detail.', error)

                if (!isMounted) {
                    return
                }

                setErrorType(
                    error instanceof ApiError && error.status === 404
                        ? 'not-found'
                        : 'network',
                )
            } finally {
                if (isMounted) {
                    setIsLoading(false)
                }
            }
        }

        loadPlanDetail()

        return () => {
            isMounted = false
        }
    }, [planId])

    useEffect(() => {
        let isMounted = true

        async function loadPlanTodos() {
            if (!Number.isInteger(numericPlanId) || numericPlanId < 1) {
                if (isMounted) {
                    setTodos([])
                    setTodoErrorMessage('')
                }
                return
            }

            try {
                setIsTodoLoading(true)
                setTodoErrorMessage('')

                const response = await getPlanTodos(numericPlanId)

                if (isMounted) {
                    setTodos(response)
                }
            } catch (error) {
                console.error('Failed to load plan todos.', error)

                if (isMounted) {
                    setTodos([])
                    setTodoErrorMessage(
                        'TODOの取得に失敗しました。時間をおいて再度お試しください。',
                    )
                }
            } finally {
                if (isMounted) {
                    setIsTodoLoading(false)
                }
            }
        }

        loadPlanTodos()

        return () => {
            isMounted = false
        }
    }, [numericPlanId])

    return (
        <main className="page plan-edit-page">
            <header className="plan-edit-header">
                <div>
                    <h1>{planDetail?.planName || 'プラン編集'}</h1>

                    {!isLoading && !errorType && planDetail && (
                        <div className="plan-summary" aria-label="プラン概要">
                            <strong>{formatPlanPeriod(planDetail)}</strong>
                            <span className="plan-duration-badge">
                                {planDetail.dayCount}日間
                            </span>
                        </div>
                    )}

                </div>

                <button
                    className="button button-secondary button-top-right"
                    type="button"
                    onClick={() => navigate('/plans')}
                >
                    ← プラン一覧へ戻る
                </button>
            </header>

            {isLoading && (
                <p className="status-message">
                    プラン詳細を読み込んでいます。
                </p>
            )}

            {!isLoading && errorType === 'not-found' && (
                <section className="empty-state" role="alert">
                    <h2>指定されたプランは見つかりませんでした</h2>
                    <p>プラン一覧へ戻り、対象のプランを選択してください。</p>
                </section>
            )}

            {!isLoading && errorType === 'network' && (
                <section
                    className="status-message status-message-error"
                    role="alert"
                >
                    <h2>プラン詳細の取得に失敗しました</h2>
                    <p>時間をおいて再度お試しください。</p>
                </section>
            )}

            {!isLoading && !errorType && planDetail && (
                <>
                    <div
                        className={`plan-editor-layout ${
                            isTodoPanelOpen
                                ? 'plan-editor-layout-todo-open'
                                : 'plan-editor-layout-todo-collapsed'
                        }`}
                    >
                        <section
                            className="plan-itinerary-panel"
                            aria-label={`${planDetail.planName}の行程`}
                        >

                            <div className="itinerary-days">
                                {planDetail.days.map((day) => (
                                    <PlanDayRow key={day.dayNumber} day={day} />
                                ))}
                            </div>

                            <div className="comparison-legend" aria-label="Block種別の凡例">
            <span>
                <i className="legend-color legend-color-activity" />
                アクティビティ
            </span>
                                <span>
                <i className="legend-color legend-color-transfer" />
                移動
            </span>
                            </div>
                        </section>

                        <div className="plan-editor-side-column">
                            <aside
                                className="candidate-panel"
                                aria-label="候補Blockエリア"
                            >
                                <div className="candidate-panel-header">
                                    <h2>候補Block</h2>
                                </div>

                                <p className="candidate-panel-message">
                                    行程に配置されていない候補Blockを表示
                                </p>
                            </aside>

                            <PlanTodoPanel
                                isOpen={isTodoPanelOpen}
                                onToggle={() => setIsTodoPanelOpen((isOpen) => !isOpen)}
                                isLoading={isTodoLoading}
                                errorMessage={todoErrorMessage}
                                todos={todos}
                            />
                        </div>
                    </div>
                </>
            )}
        </main>
    )
}