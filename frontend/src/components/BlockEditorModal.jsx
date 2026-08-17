import { useCallback, useEffect, useState } from "react";

import { ApiError } from '../api/client'
import {
    createBlock,
    getBlock,
    updateBlock,
} from '../api/blocks'

const EMPTY_FORM = {
    blockType: 'activity',
    blockName: '',
    blockPlace: '',
    blockDetails: '',

    activityType: '',
    activityCost: '',
    activityDuration: '',

    transferDeparture: '',
    transferArrival: '',
    transferMethod: '',
    transferCost: '',
    transferDuration: '',
    transferDepartureTime: '',
    transferArrivalTime: '',
}

function createFormFromBlock(block) {
    return {
        blockType: block.blockType,
        blockName: block.blockName ?? '',
        blockPlace: block.blockPlace ?? '',
        blockDetails: block.blockDetails ?? '',

        activityType: block.activityType ?? '',
        activityCost: block.activityCost ?? '',
        activityDuration: block.activityDuration ?? '',

        transferDeparture: block.transferDeparture ?? '',
        transferArrival: block.transferArrival ?? '',
        transferMethod: block.transferMethod ?? '',
        transferCost: block.transferCost ?? '',
        transferDuration: block.transferDuration ?? '',
        transferDepartureTime: toTimeInputValue(block.transferDepartureTime),
        transferArrivalTime: toTimeInputValue(block.transferArrivalTime),
    }
}

// 時刻データの先頭から5文字を取り出す (12:34:56 -> 12:34)
function toTimeInputValue(value) {
    return value ? value.slice(0, 5) : ''
}

function toOptionalText(value) {
    const trimmedValue = value.trim()

    return trimmedValue === '' ? null : trimmedValue
}

function toOptionalNumber(value) {
    return value === '' ? null : Number(value)
}

function toApiTime(value) {
    return value === '' ? null : `${value}:00`
}

function getLoadErrorMessage(error) {
    if (error instanceof ApiError && error.status === 400) {
        return 'ブロック詳細を取得できませんでした。ブロックIDを確認してください。'
    }

    if (error instanceof ApiError && error.status === 404) {
        return '対象のブロックは見つかりませんでした。既に削除された可能性があります。'
    }

    return 'ブロック詳細の取得に失敗しました。時間をおいて再度お試しください。'
}

function createPayload(form) {
    const commonFields = {
        blockName: form.blockName.trim(),
        blockPlace: toOptionalText(form.blockPlace),
        blockDetails: toOptionalText(form.blockDetails),
    }

    if (form.blockType === 'activity') {
        return {
            ...commonFields,
            activityType: form.activityType.trim(),
            activityCost: toOptionalNumber(form.activityCost),
            activityDuration: toOptionalNumber(form.activityDuration),
        }
    }

    if (form.blockType === 'transfer') {
        return {
            ...commonFields,
            transferDeparture: form.transferDeparture.trim(),
            transferArrival: form.transferArrival.trim(),
            transferMethod: toOptionalText(form.transferMethod),
            transferCost: toOptionalNumber(form.transferCost),
            transferDuration: toOptionalNumber(form.transferDuration),
            transferDepartureTime: toApiTime(form.transferDepartureTime),
            transferArrivalTime: toApiTime(form.transferArrivalTime),
        }
    }
}

function validateForm(form) {
    if (form.blockName.trim() === '') {
        return 'ブロック名を入力してください。'
    }

    if (form.blockType === 'activity' && form.activityType.trim() === '') {
        return 'アクティビティタイプを入力してください。'
    }

    if (form.blockType === 'transfer' && form.transferDeparture.trim() === '') {
        return '出発地を入力してください'
    }

    if (form.blockType === 'transfer' && form.transferArrival.trim() === '') {
        return '到着地を入力してください'
    }

    return ''
}

function getSaveErrorMessage(error) {
    if (error instanceof ApiError && error.status === 400) {
        return '入力内容に誤りがあります。必須項目と入力値を確認してください。'
    }

    if (error instanceof ApiError && error.status === 404) {
        return '対象のブロックは見つかりませんでした。画面を再読み込みしてください。'
    }

    return '保存に失敗しました。通信状態を確認して、再度お試しください。'
}

export default function BlockEditorModal({
    mode,
    blockId = null,
    onClose,
    onSaved,
    onSavingChange,
}) {
    const isEditMode = mode === 'edit'

    const [form, setForm] = useState(EMPTY_FORM)
    const [usages, setUsages] = useState([])
    const [isLoading, setIsLoading] = useState(isEditMode)
    const [loadErrorMessage, setLoadErrorMessage] = useState('')
    const [saveErrorMessage, setSaveErrorMessage] = useState('')
    const [isSaving, setIsSaving] = useState(false)

    const loadBlock = useCallback(async (id) => {
        if (!Number.isInteger(id) || id < 1) {
            setLoadErrorMessage('編集対象のブロックが不正です。')
            setIsLoading(false)
            return
        }

        try {
            setIsLoading(true)
            setLoadErrorMessage('')
            setSaveErrorMessage('')

            const block = await getBlock(id)

            setForm(createFormFromBlock(block))
            setUsages(block.usages ?? [])
        } catch (error) {
            console.error('Failed to load block detail.', error)
            setLoadErrorMessage(getLoadErrorMessage(error))
        } finally {
            setIsLoading(false)
        }
    }, [])

    useEffect(() => {
        if (isEditMode) {
            void loadBlock(blockId)
            return
        }

        setForm(EMPTY_FORM)
        setUsages([])
        setIsLoading(false)
        setLoadErrorMessage('')
        setSaveErrorMessage('')
    }, [isEditMode, blockId, loadBlock])

    useEffect(() => {
        return () => {
            onSavingChange?.(false)
        }
    }, [onSavingChange])

    const isFormDisabled = isLoading || isSaving
    const sharedPlanNames = usages
        .map((usage) => usage.planName)
        .filter(Boolean)
        .join(', ')


    function handleFieldChange(event) {
        const { name, value } = event.target

        setForm((currentForm) => ({
            ...currentForm,
            [name]: value,
        }))
    }

    function handleBlockTypeChange(blockType) {
        if (isEditMode || isLoading || isSaving) {
            return
        }

        setForm((currentForm) => ({
            ...currentForm,
            blockType,
        }))
        setSaveErrorMessage('')
    }

    async function handleSubmit(event) {
        event.preventDefault()

        const validationMessage = validateForm(form)

        if (validationMessage) {
            setSaveErrorMessage(validationMessage)
            return
        }

        const payload = createPayload(form)

        try {
            setIsSaving(true)
            onSavingChange?.(true)
            setSaveErrorMessage('')

            const savedBlock = isEditMode
                ? await updateBlock(blockId, payload)
                : await createBlock({
                    blockType: form.blockType,
                    ...payload,
                })

            await onSaved?.({
                mode,
                block: savedBlock,
            })

            onClose()
        } catch (error) {
            console.error('Failed to save block.', error)
            setSaveErrorMessage(getSaveErrorMessage(error))
        } finally {
            setIsSaving(false)
            onSavingChange?.(false)
        }
    }

    return (
        <div className="modal-overlay" role="presentation">
            <section
                className="block-editor-modal"
                role="dialog"
                aria-modal="true"
                aria-labelledby="block-editor-modal-title"
            >
                <header className="block-editor-modal-header">
                    <div>
                        <h2 id="block-editor-modal-title">
                            {isEditMode ? 'ブロックを編集' : '新規ブロックを作成'}
                        </h2>
                        {isEditMode && !isLoading && !loadErrorMessage && (
                            <p className="block-editor-modal-subtitle">
                                ブロック種別：{
                                form.blockType === 'activity'
                                    ? 'アクティビティ'
                                    : '移動'
                            }
                            </p>
                        )}
                    </div>

                    <button
                        className="block-editor-close-button"
                        type="button"
                        onClick={onClose}
                        disabled={isSaving}
                        aria-label="モーダルを閉じる"
                    >
                        ×
                    </button>
                </header>

                <form onSubmit={handleSubmit}>
                    <div className="block-editor-modal-body">
                        {isLoading && (
                            <p className="block-editor-status-message">
                                ブロック詳細を読み込んでいます。
                            </p>
                        )}

                        {!isLoading && loadErrorMessage && (
                            <section
                                className="block-editor-error-message"
                                role="alert"
                            >
                                <p>{loadErrorMessage}</p>

                                <div className="block-editor-error-actions">
                                    <button
                                        className="button button-secondary"
                                        type="button"
                                        onClick={() => void loadBlock(blockId)}
                                    >
                                        再読み込み
                                    </button>

                                    <button
                                        className="button button-secondary"
                                        type="button"
                                        onClick={onClose}
                                    >
                                        閉じる
                                    </button>
                                </div>
                            </section>
                        )}

                        {!isLoading && !loadErrorMessage && (
                            <>
                                {!isEditMode && (
                                    <fieldset
                                        className="block-type-selector"
                                        disabled={isFormDisabled}
                                    >
                                        <legend>ブロック種別</legend>

                                        <div className="block-type-selector-buttons">
                                            <button
                                                className={`block-type-button ${
                                                    form.blockType === 'activity'
                                                        ? 'block-type-button-activity-selected'
                                                        : ''
                                                }`}
                                                type="button"
                                                onClick={() =>
                                                    handleBlockTypeChange(
                                                        'activity',
                                                    )
                                                }
                                                aria-pressed={
                                                    form.blockType === 'activity'
                                                }
                                            >
                                                アクティビティ
                                            </button>

                                            <button
                                                className={`block-type-button ${
                                                    form.blockType === 'transfer'
                                                        ? 'block-type-button-transfer-selected'
                                                        : ''
                                                }`}
                                                type="button"
                                                onClick={() =>
                                                    handleBlockTypeChange(
                                                        'transfer',
                                                    )
                                                }
                                                aria-pressed={
                                                    form.blockType === 'transfer'
                                                }
                                            >
                                                移動
                                            </button>
                                        </div>
                                    </fieldset>
                                )}

                                {isEditMode && (
                                    <section
                                        className="block-shared-notice"
                                        aria-label="ブロック共有に関する注意"
                                    >
                                        {usages.length > 0 ? (
                                            <>
                                                <strong>
                                                    このブロックを編集すると、使用中の
                                                    {usages.length}プランすべてに変更が反映されます。
                                                </strong>

                                                {sharedPlanNames && (
                                                    <span>
                                                        使用中：{sharedPlanNames}
                                                    </span>
                                                )}
                                            </>
                                        ) : (
                                            <span>
                                                このブロックは現在どのプランにも配置されていません。
                                            </span>
                                        )}
                                    </section>
                                )}

                                {saveErrorMessage && (
                                    <p
                                        className="block-editor-error-message"
                                        role="alert"
                                    >
                                        {saveErrorMessage}
                                    </p>
                                )}

                                <div className="block-editor-field">
                                    <label htmlFor="block-name">
                                        ブロック名
                                        <span
                                            className="block-editor-required"
                                            aria-hidden="true"
                                        >
                                            *
                                        </span>
                                    </label>

                                    <input
                                        id="block-name"
                                        name="blockName"
                                        type="text"
                                        value={form.blockName}
                                        onChange={handleFieldChange}
                                        disabled={isFormDisabled}
                                        maxLength="100"
                                        required
                                    />
                                </div>

                                <div className="block-editor-field">
                                    <label htmlFor="block-place">場所</label>

                                    <input
                                        id="block-place"
                                        name="blockPlace"
                                        type="text"
                                        value={form.blockPlace}
                                        onChange={handleFieldChange}
                                        disabled={isFormDisabled}
                                        maxLength="255"
                                    />
                                </div>

                                <div className="block-editor-field">
                                    <label htmlFor="block-details">詳細</label>

                                    <textarea
                                        id="block-details"
                                        name="blockDetails"
                                        value={form.blockDetails}
                                        onChange={handleFieldChange}
                                        disabled={isFormDisabled}
                                        rows="4"
                                    />
                                </div>

                                {form.blockType === 'activity' ? (
                                    <ActivityFields
                                        form={form}
                                        disabled={isFormDisabled}
                                        onChange={handleFieldChange}
                                    />
                                ) : (
                                    <TransferFields
                                        form={form}
                                        disabled={isFormDisabled}
                                        onChange={handleFieldChange}
                                    />
                                )}
                            </>
                        )}
                    </div>

                    {!isLoading && !loadErrorMessage && (
                        <footer className="block-editor-modal-footer">
                            <button
                                className="button button-secondary"
                                type="button"
                                onClick={onClose}
                                disabled={isSaving}
                            >
                                キャンセル
                            </button>

                            <button
                                className="button button-primary"
                                type="submit"
                                disabled={isSaving}
                            >
                                {isSaving
                                    ? '保存しています...'
                                    : isEditMode
                                        ? '変更を保存'
                                        : 'ブロックを作成'}
                            </button>
                        </footer>
                    )}
                </form>
            </section>
        </div>
    )
}

function ActivityFields({ form, disabled, onChange }) {
    return (
        <section className="block-editor-type-fields">
            <div className="block-editor-field block-editor-field-compact">
                <label htmlFor='activity-type'>
                    アクティビティタイプ
                    <span
                        className="block-editor-required"
                        aria-hidden="true"
                    >
                        *
                    </span>
                </label>

                <select
                    id="activity-type"
                    name="activityType"
                    value={form.activityType}
                    onChange={onChange}
                    disabled={disabled}
                    required
                >
                    <option valaue="" disabled>未選択</option>
                    <option value="観光">🗼 観光</option>
                    <option value="食事">🍴 食事</option>
                    <option value="宿泊">🏨 宿泊</option>
                    <option value="買い物">🛍️ 買い物</option>
                    <option value="体験">🎨 体験</option>
                    <option value="その他">❓ その他</option>
                </select>
            </div>

            <div className="block-editor-field-row">
                <div className="block-editor-field">
                    <label htmlFor="activity-cost">費用</label>

                    <input
                        id="activity-cost"
                        name="activityCost"
                        type="number"
                        value={form.activityCost}
                        onChange={onChange}
                        disabled={disabled}
                        min="0"
                        step="0.01"
                        inputMode="decimal"
                    />
                </div>

                <div className="block-editor-field">
                    <label htmlFor="activity-duration">
                        所要時間（分）
                    </label>

                    <input
                        id="activity-duration"
                        name="activityDuration"
                        type="number"
                        value={form.activityDuration}
                        onChange={onChange}
                        disabled={disabled}
                        min="0"
                        step="1"
                        inputMode="numeric"
                    />
                </div>
            </div>
        </section>
    )
}

function TransferFields({ form, disabled, onChange }) {
    return (
        <section className="block-editor-type-fields">
            <div className="block-editor-field-row">
                <div className="block-editor-field">
                    <label htmlFor="transfer-departure">
                        出発地
                        <span
                            className="block-editor-required"
                            aria-hidden="true"
                        >
                            *
                        </span>
                    </label>

                    <input
                        id="transfer-departure"
                        name="transferDeparture"
                        type="text"
                        value={form.transferDeparture}
                        onChange={onChange}
                        disabled={disabled}
                        maxLength="255"
                        required
                    />
                </div>

                <div className="block-editor-field">
                    <label htmlFor="transfer-arrival">
                        到着地
                        <span
                            className="block-editor-required"
                            aria-hidden="true"
                        >
                            *
                        </span>
                    </label>

                    <input
                        id="transfer-arrival"
                        name="transferArrival"
                        type="text"
                        value={form.transferArrival}
                        onChange={onChange}
                        disabled={disabled}
                        maxLength="255"
                        required
                    />
                </div>
            </div>

            <div className="block-editor-field block-editor-field-compact">
                <label htmlFor="transfer-method">移動手段</label>

                <select
                    id="transfer-method"
                    name="transferMethod"
                    value={form.transferMethod}
                    onChange={onChange}
                    disabled={disabled}
                >
                    <option value="">未選択</option>
                    <option value="徒歩">🚶 徒歩</option>
                    <option value="自転車">🚲 自転車</option>
                    <option value="バス">🚌 バス</option>
                    <option value="電車">🚃 電車</option>
                    <option value="飛行機">✈️ 飛行機</option>
                    <option value="船">⛴️ 船</option>
                    <option value="その他">❓ その他</option>
                </select>
            </div>

            <div className="block-editor-field-row">
                <div className="block-editor-field">
                    <label htmlFor="transfer-cost">費用</label>

                    <input
                        id="transfer-cost"
                        name="transferCost"
                        type="number"
                        value={form.transferCost}
                        onChange={onChange}
                        disabled={disabled}
                        min="0"
                        step="0.01"
                        inputMode="decimal"
                    />
                </div>

                <div className="block-editor-field">
                    <label htmlFor="transfer-duration">
                        所要時間（分）
                    </label>

                    <input
                        id="transfer-duration"
                        name="transferDuration"
                        type="number"
                        value={form.transferDuration}
                        onChange={onChange}
                        disabled={disabled}
                        min="0"
                        step="1"
                        inputMode="numeric"
                    />
                </div>
            </div>

            <div className="block-editor-field-row">
                <div className="block-editor-field">
                    <label htmlFor="transfer-departure-time">出発時刻</label>

                    <input
                        id="transfer-departure-time"
                        name="transferDepartureTime"
                        type="time"
                        value={form.transferDepartureTime}
                        onChange={onChange}
                        disabled={disabled}
                    />
                </div>

                <div className="block-editor-field">
                    <label htmlFor="transfer-arrival-time">到着時刻</label>

                    <input
                        id="transfer-arrival-time"
                        name="transferArrivalTime"
                        type="time"
                        value={form.transferArrivalTime}
                        onChange={onChange}
                        disabled={disabled}
                    />
                </div>
            </div>
        </section>
    )
}
