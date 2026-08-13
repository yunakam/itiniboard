import { useEffect, useMemo, useState } from 'react'
import { getPlan, getPlans, getPlanTodos } from '../api/plans'
import PlanComparisonTable from '../components/PlanComparisonTable.jsx'
import TodoDrawer from '../components/TodoDrawer'

export default function PlanListPage() {
    const [plans, setPlans] = useState([])
    const [planDetails, setPlanDetails] = useState({})
    const [selectedPlanId, setSelectedPlanId] = useState(null)
    const [selectedPlanTodos, setSelectedPlanTodos] = useState([])
    const [isLoading, setIsLoading] = useState(true)
    const [isTodoLoading, setIsTodoLoading] = useState(false)
    const [isTodoDrawerOpen, setIsTodoDrawerOpen] = useState(true)
    const [errorMessage, setErrorMessage] = useState('')
    const [displayOptions, setDisplayOptions] = useState({
        locale: 'ja-JP',
        currency: 'JPY',
    })

    useEffect(() => {
        let isMounted = true

        async function loadComparisonPlans() {
            try {
                setIsLoading(true)
                setErrorMessage('')

                const planList = await getPlans()
                const details = await Promise.all(
                    planList.map((plan) => getPlan(plan.planId)),
                )

                if (!isMounted) {
                    return
                }

                const detailsByPlanId = Object.fromEntries(
                    details.map((planDetail) => [planDetail.planId, planDetail]),
                )

                setPlans(planList)
                setPlanDetails(detailsByPlanId)
                setSelectedPlanId(planList[0]?.planId ?? null)
            } catch (error) {
                console.error('Failed to load plan comparison data.', error)

                if (isMounted) {
                    setErrorMessage(
                        error.message ||
                        'プランデータの取得に失敗しました。時間をおいて再度お試しください。',
                    )
                }
            } finally {
                if (isMounted) {
                    setIsLoading(false)
                }
            }
        }

        loadComparisonPlans()

        return () => {
            isMounted = false
        }
    }, [])

    useEffect(() => {
        let isMounted = true

        async function loadSelectedPlanTodos() {
            if (selectedPlanId === null) {
                setSelectedPlanTodos([])
                return
            }

            try {
                setIsTodoLoading(true)
                const todos = await getPlanTodos(selectedPlanId)

                if (isMounted) {
                    setSelectedPlanTodos(todos)
                }
            } catch (error) {
                console.error('Failed to load selected plan todos.', error)

                if (isMounted) {
                    setSelectedPlanTodos([])
                }
            } finally {
                if (isMounted) {
                    setIsTodoLoading(false)
                }
            }
        }

        loadSelectedPlanTodos()

        return () => {
            isMounted = false
        }
    }, [selectedPlanId])

    const selectedPlan = useMemo(
        () => plans.find((plan) => plan.planId === selectedPlanId) ?? null,
        [plans, selectedPlanId],
    )

    return (
        <>
            <main className="page"
                // Todo drawerを一覧表に重ねない
                // className={`page ${isTodoDrawerOpen ? '' : 'page-drawer-closed'}`}
            >
                <section className="page-header">
                    <div>
                        <h1>プラン一覧</h1>
                        {/*<p className="page-description">*/}
                        {/*    候補プランの日別行程、費用、日数を比較できます*/}
                        {/*</p>*/}
                    </div>

                    <button
                        className="button button-primary button-create-plan"
                        type="button"
                        disabled
                    >
                        ＋ 新規プランを作成
                    </button>
                </section>

                {isLoading && (
                    <p className="status-message">
                        プラン比較データを読み込んでいます。
                    </p>
                )}

                {!isLoading && errorMessage && (
                    <p className="status-message status-message-error" role="alert">
                        {errorMessage}
                    </p>
                )}

                {!isLoading && !errorMessage && plans.length === 0 && (
                    <section className="empty-state">
                        <h2>保存済みのプランはありません</h2>
                        <p>「新規プランを作成」から旅行プランを作成してください。</p>
                    </section>
                )}

                {!isLoading && !errorMessage && plans.length > 0 && (
                    <>
                        <PlanComparisonTable
                            plans={plans}
                            planDetails={planDetails}
                            selectedPlanId={selectedPlanId}
                            onSelectPlan={setSelectedPlanId}
                            isTodoDrawerOpen={isTodoDrawerOpen}
                            displayOptions={displayOptions}
                        />

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
                    </>
                )}
            </main>

            {selectedPlan && (
                <>
                    <button
                        className={`todo-toggle ${
                            isTodoDrawerOpen ? 'todo-toggle-open' : 'todo-toggle-closed'
                        }`}
                        type="button"
                        aria-label={
                            isTodoDrawerOpen ? 'TODOを閉じる' : 'TODOを表示'
                        }
                        title={isTodoDrawerOpen ? 'TODOを閉じる' : 'TODOを表示'}
                        onClick={() => setIsTodoDrawerOpen((isOpen) => !isOpen)}
                    >
                        {!isTodoDrawerOpen && <span>TODO</span>}
                        <span aria-hidden="true">{isTodoDrawerOpen ? '›' : '‹'}</span>
                    </button>

                    <TodoDrawer
                        isOpen={isTodoDrawerOpen}
                        isLoading={isTodoLoading}
                        planName={selectedPlan.planName}
                        todos={selectedPlanTodos}
                        displayOptions={displayOptions}
                    />
                </>
            )}
        </>
    )
}