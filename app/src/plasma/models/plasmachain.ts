import { PlasmaBlock } from './plasmablock';

export interface PlasmaChain {
    address: string;
    blocks: Array<PlasmaBlock>;
    depositBlocks: Array<PlasmaBlock>;
    allTransactions: number;
    childrenTotalTransactions: number;
    parentBlocks: number;
    childrenRootTransactions: number;
    numberOfUTXOs: number;
}
