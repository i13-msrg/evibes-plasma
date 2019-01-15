export interface Configuration {
    numberOfEthereumNodes: number;
    numberOfPlasmaClients: number;
    tokensPerClient: number;
    transactionsPerPlasmaBlock: number;
    plasmaChildren: number;
    numberOfPeers: number;
    blockGasLimit: number;
    transactionGas: number;
    transactionGasPrice: number;
    transactionGenerationRate: number;
    numberOfTransactionGenerationIntervals: number;
    enableExternalTransactions: boolean;
    externalGasLimit: number;
    externalGasPrice: number;
    externalTransactionGenerationRate: number;
    numberOfEthereumExternalAccounts: number;
    amountPerEthereumExternalAccount: number;
}
