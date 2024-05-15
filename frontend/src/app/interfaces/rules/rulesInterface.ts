export interface Rules {
  minRestPeriod: MinRestPeriod | null,
  maximumShiftLengths: MaximumShiftLength | null,
  mandatoryOffDays: MandatoryOffDays | null,
}

export interface MinRestPeriod {
  duration: number;
}

export interface MaximumShiftLength {
  duration: number;
}

export interface MandatoryOffDays {
  numberOfDaysInMonth: number;
}

export interface duration {
  timeAmount: number,
  period: timePeriod,
}

export enum timePeriod {
  minutes,
  days,
  weeks,
  months,
  years
}
