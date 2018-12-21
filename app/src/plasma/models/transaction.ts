export interface Transaction {
    nonce: number;
    from: string;
    to: string;
    amount: number;
    data: any;
}
