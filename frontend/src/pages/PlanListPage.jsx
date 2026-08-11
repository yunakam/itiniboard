import { useEffect, useState } from 'react'
import { getPlans } from '../api/plans'

function formatDate(dateString) {
    if (!dateString) {
        return '未設定'
    }

    return new Intl.DateTimeFormat('ja-JP', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
    }).format(new Date(`${dateString}T00:00:00`))
}

export default function PlanListPage() {
    const [plans, setPlans] = useState([])
    const [isLoading, setIsLoading] = useState(true)
    const [errorMessage, setErrorMessage] = useState('')

    useEffect(() => {
        let isMounted = true

        async function loadPlans() {
            try {
                setIsLoading(true)
                setErrorMessage('')

                const response = await getPlans()

                if (isMounted) {
                    setPlans(response)
                }
            } catch (error) {
                console.error('Failed to load plans.', error)

                if (isMounted) {
                    setErrorMessage(
                        error.message ||
                        'プラン一覧の取得に失敗しました。時間をおいて再度お試しください。',
                    )
                }
            } finally {
                if (isMounted) {
                    setIsLoading(false)
                }
            }
        }

        loadPlans()

        return () => {
            isMounted = false
        }
    }, [])

    return (
        <main className="page">
            <section className="page-header">
                <div>
                    <h1>保存済プラン一覧</h1>
                    <p className="page-description">
                        旅行プランの費用と日数を比較できます
                    </p>
                </div>

                <button className="button button-primary" type="button" disabled>
                    ＋ 新規プランを作成
                </button>
            </section>

            {isLoading && <p className="status-message">プランを読み込んでいます。</p>}

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
                <section aria-label="保存済プラン">
                    <ul className="plan-list">
                        {plans.map((plan) => (
                            <li key={plan.planId} className="plan-card">
                                <h2>{plan.planName}</h2>
                                <p>
                                    {formatDate(plan.planStartDate)} 〜{' '}
                                    {formatDate(plan.planEndDate)}
                                </p>
                            </li>
                        ))}
                    </ul>
                </section>
            )}
        </main>
    )
}