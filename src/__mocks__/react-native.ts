export const Platform = {
  OS: 'ios',
  select: (spec: Record<string, any>) => spec.ios || spec.default,
}
