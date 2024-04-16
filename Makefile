HANDLER=ordertracker.MainApp

.PHONY: clean
clean:
	rm -rf app app.build_artifacts.txt
	scala-cli clean .

.PHONY: build-native
build-native:
	scala-cli \
		--power package \
		--main-class $(HANDLER) \
		--native-image . \
		-o app \
		-- \
		--no-fallback \
		--enable-url-protocols=http \
		--trace-class-initialization=io.netty.channel.DefaultFileRegion \
		--trace-class-initialization=io.netty.util.AbstractReferenceCounted \
		--initialize-at-build-time=org.slf4j.LoggerFactory \
		--initialize-at-run-time=io.netty.channel.kqueue.KQueue \
		--verbose
