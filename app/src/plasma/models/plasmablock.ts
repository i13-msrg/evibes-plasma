export interface PlasmaBlock {
    number: number;
    prevBlockNum: number;
    prevBlockHash: string;
    merkleRoot: string;
    timestamp: number;
    depositBlock: boolean;
}
