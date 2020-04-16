export class Token {
  id: number;
  name: string;
  symbol: string;
  balance: number;
  cap: number;
  quantity: number;
  owner: string;
  template: string;
  decimals: number;
  paused: boolean;
  transferable: boolean;
  burnable: boolean;
  mintable: boolean;
  divisible: boolean;
}
