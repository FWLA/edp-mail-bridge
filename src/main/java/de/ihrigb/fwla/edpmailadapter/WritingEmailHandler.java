package de.ihrigb.fwla.edpmailadapter;

import java.util.List;

import de.ihrigb.fwla.mail.Email;
import de.ihrigb.fwla.mail.EmailHandler;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class WritingEmailHandler implements EmailHandler<String> {

	private final FileWriter fileWriter;
	private final ValueExtractor defaultExtractor;
	private final List<ValueExtractor> valueExtractors;

	@Override
	public void handle(Email<String> email) {
		this.valueExtractors.stream().filter(ve -> ve.isApplicable(email)).findFirst()
				.orElse(this.defaultExtractor).extract(email, fileWriter::write);
	}
}
