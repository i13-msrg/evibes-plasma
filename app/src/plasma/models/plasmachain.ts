import { PlasmaBlock } from './plasmablock';

export interface PlasmaChain {
    address: string;
    blocks: Array<PlasmaBlock>;
    depositBlocks: Array<PlasmaBlock>;
    allTransactions: number;
    childrenRootTransactions: number;
}
