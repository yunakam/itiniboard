export const DEFAULT_DISPLAY_OPTIONS = {
    locale: 'ja-JP',
    currency: 'JPY',
}

function toLocalDate(dateString) {
    return new Date(`${dateString}T00:00:00`)
}

export function formatCurrency(
    value,
    { locale, currency } = DEFAULT_DISPLAY_OPTIONS,
) {
    return new Intl.NumberFormat(locale, {
        style: 'currency',
        currency,
        maximumFractionDigits: 0,
    }).format(Number(value ?? 0))
}

export function formatMonthDay(
    dateString,
    { locale } = DEFAULT_DISPLAY_OPTIONS,
) {
    return new Intl.DateTimeFormat(locale, {
        month: 'numeric',
        day: 'numeric',
    }).format(toLocalDate(dateString))
}

export function formatWeekday(
    dateString,
    { locale } = DEFAULT_DISPLAY_OPTIONS,
) {
    return new Intl.DateTimeFormat(locale, {
        weekday: 'long',
    }).format(toLocalDate(dateString))
}

export function formatDeadline(
    dateString,
    { locale } = DEFAULT_DISPLAY_OPTIONS,
) {
    if (!dateString) {
        return '期限未設定'
    }

    return new Intl.DateTimeFormat(locale, {
        month: 'numeric',
        day: 'numeric',
    }).format(toLocalDate(dateString))
}