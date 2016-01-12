# IOT Recorder

##What is it?

A tool for recording video of IOT objects and their data in sync

##How does it work?
	
1. An IOT object passivley broadcasts information (ex. Temperature, Labels, embbeded text etc.) 
2. Our IOT recording app detects IOT objects on screen.
3. The user records a video and the app simultaneously recorders the data.

##Technical details:
	
###Hardware
	
- Photon - Wirelessly enabled board for data communication [Buy here!](https://store.particle.io/collections/photon)
- Load Cell and Load Cell Amplifier - Produces readable data for the Photon [Buy load cell here!](https://www.sparkfun.com/products/13329), [Buy amplifier here!](https://www.sparkfun.com/products/13230)

###Software
	
- Photon API - Communicates with phone [See here!](https://docs.particle.io/reference/firmware/photon/)
- Deep Belief SDK - Detects IOT objects [See here!](https://github.com/jetpacapp/DeepBeliefSDK)

##More references:
	
- [Hardware Hookup](https://learn.sparkfun.com/tutorials/load-cell-amplifier-hx711-breakout-hookup-guide?_ga=1.53177064.1747307081.1440772503)
- [Understanding Neural Networks](http://neuralnetworksanddeeplearning.com/)